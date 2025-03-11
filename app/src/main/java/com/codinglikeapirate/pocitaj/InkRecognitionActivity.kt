package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import com.google.mlkit.vision.digitalink.Ink.Stroke
import com.google.mlkit.vision.digitalink.RecognitionContext
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke as GraphicsStroke


class ExerciseBookViewModel : ViewModel() {
    private val _exerciseBook: MutableState<ExerciseBook> = mutableStateOf(ExerciseBook())
    private var _exerciseIndex = 0

    fun nextExercise() { ++_exerciseIndex }
    fun currentExercise(): ExerciseBook.Exercise {
        return _exerciseBook.value.historyList[_exerciseIndex]
    }

    init {
        _exerciseBook.value.generate()
        _exerciseBook.value.generate()
        _exerciseBook.value.generate()
    }
}

class InkRecognitionActivity : ComponentActivity() {

    @JvmField
    @VisibleForTesting
    var modelManager =
        ModelManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ExerciseBookViewModel by viewModels()
        modelManager.setModel("en-US")

        setContent {
            AppTheme {
                InkRecognitionScreen(Modifier, modelManager, viewModel)
            }
        }
    }
}

@Composable
fun InkRecognitionScreen(
    modifier: Modifier = Modifier,
    modelManager: ModelManager? = null,
    exerciseBookViewModel: ExerciseBookViewModel
) {
    val recognitionDelayMillis = 1000L

    var recognizedText by remember { mutableStateOf("") }
    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentStrokeBuilder = remember { Stroke.builder() }
    var inkBuilder by remember { mutableStateOf(Ink.builder()) }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }
    var isDrawing by remember { mutableStateOf(false) }

    val backgroundAll = ImageBitmap.imageResource(id = R.drawable.paper_top)
    val backgroundAnswer = ImageBitmap.imageResource(id = R.drawable.paper_answer)

    LaunchedEffect(key1 = isDrawing) {
        if (!isDrawing && inkBuilder.build().strokes.isNotEmpty()) {
            delay(recognitionDelayMillis)
            recognizeInk(modelManager!!, inkBuilder.build()) { result ->
                recognizedText = result
                // Reset the ink so the next recognized value doesn't include already
                // recognized characters.
                paths.clear()
                inkBuilder = Ink.builder()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .drawBehind {
                drawImage(
                    image = backgroundAll,
                    dstOffset = IntOffset(0, 0),
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )
            }
    ) {
        Text(
            text = "12 + 14",
            modifier = Modifier
                .fillMaxWidth()
                .height(312.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 96.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .drawBehind {
                    drawImage(
                        image = backgroundAnswer,
                        dstOffset = IntOffset(0, 0),
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDrawing = true
                            currentPath.value.moveTo(offset.x, offset.y)
                            currentStrokeBuilder.addPoint(Point.create(offset.x, offset.y))
                            currentPathPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
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
        InkRecognitionScreen(Modifier, null, viewModel())
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

    modelManager.recognizer!!.recognize(
        ink,
        RecognitionContext.builder().setPreContext("1234").build()
    )
        .addOnSuccessListener { result ->
            val recognizedText = result.candidates.firstOrNull()?.text ?: ""
            onResult(recognizedText)
        }
        .addOnFailureListener { e: Exception ->
            Log.e("InkRecognition", "Error during recognition", e)
            onResult("Recognition failed")
        }
}