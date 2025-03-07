package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke as GraphicsStroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import com.google.mlkit.vision.digitalink.Ink.Stroke
import com.google.mlkit.vision.digitalink.RecognitionContext
import kotlinx.coroutines.delay

class InkRecognitionActivity : ComponentActivity() {

    @JvmField
    @VisibleForTesting
    var modelManager =
        ModelManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        modelManager.setModel("en-US")

        setContent {
            InkRecognitionScreen(modelManager)
        }
    }
}

@Composable
fun InkRecognitionScreen(modelManager: ModelManager?) {
    val context = LocalContext.current
    val recognitionDelayMillis = 1000L

    var recognizedText by remember { mutableStateOf("") }
    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentStrokeBuilder = remember { Stroke.builder() }
    var inkBuilder = remember { Ink.builder() }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }
    var isDrawing by remember { mutableStateOf(false) }

    LaunchedEffect(isDrawing) {
        if (!isDrawing && inkBuilder.build().strokes.isNotEmpty()) {
            delay(recognitionDelayMillis)
            recognizeInk(modelManager!!, inkBuilder.build()) { result ->
                recognizedText = result
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Draw below here:",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDrawing = true
                            currentPath.value.moveTo(offset.x, offset.y)
                            currentStrokeBuilder.addPoint(Point.create(offset.x, offset.y))
                            currentPathPoints = listOf(offset)
                        },
                        onDrag = { change, dragAmount ->
                            val newOffset = change.position
                            currentPath.value.lineTo(newOffset.x, newOffset.y)
                            currentStrokeBuilder.addPoint(
                                Point.create(
                                    newOffset.x,
                                    newOffset.y
                                )
                            )
                            currentPathPoints = currentPathPoints + newOffset
                        },
                        onDragEnd = {
                            isDrawing = false
                            paths.add(Pair(currentPath.value, Color.Black))
                            currentPath.value = Path()
                            inkBuilder.addStroke(currentStrokeBuilder.build())
                            currentStrokeBuilder = Stroke.builder()
                            currentPathPoints = emptyList()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.height(300.dp)) {
                paths.forEach { (path, color) ->
                    drawPath(
                        path = path,
                        color = color,
                        style = GraphicsStroke(width = 5f)
                    )
                }
                if (currentPathPoints.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                    for (i in 1 until currentPathPoints.size) {
                        path.lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = GraphicsStroke(width = 5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                recognizeInk(modelManager!!, inkBuilder.build()) { result ->
                    recognizedText = result
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recognize")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                paths.clear()
                inkBuilder = Ink.builder()
                recognizedText = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recognized Text: $recognizedText",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun InkRecognitionScreenPreview() {
    // val modelManager = ModelManager()
    AppTheme {
        InkRecognitionScreen(null)
    }
}

private fun recognizeInk(
    modelManager: ModelManager,
    ink: Ink,
    onResult: (String) -> Unit
) {
    if (modelManager.recognizer == null) {
        onResult("Recognizer not set")
        return
    }

    modelManager.recognizer!!.recognize(ink, RecognitionContext.builder().setPreContext("1234").build())
        .addOnSuccessListener { result ->
            val recognizedText = result.candidates.firstOrNull()?.text ?: ""
            onResult(recognizedText)
        }
        .addOnFailureListener { e: Exception ->
            Log.e("InkRecognition", "Error during recognition", e)
            onResult("Recognition failed")
        }
}