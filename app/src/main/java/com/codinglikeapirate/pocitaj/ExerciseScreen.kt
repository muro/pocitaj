package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import com.google.mlkit.vision.digitalink.Ink.Stroke
import com.google.mlkit.vision.digitalink.RecognitionContext
import kotlinx.coroutines.delay

@Composable
fun InkRecognitionBox(
    modifier: Modifier = Modifier,
    modelManager: ModelManager? = null,
    hint: String,
    onAnswerSubmit: (String) -> Unit
) {
    val recognitionDelayMillis = 1000L

    var recognizedText by remember { mutableStateOf("") }
    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var currentStrokeBuilder = remember { Stroke.builder() }
    var inkBuilder by remember { mutableStateOf(Ink.builder()) }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }
    var isDrawing by remember { mutableStateOf(false) }

    val backgroundAnswer: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.paper_answer)

    val strokeColor = MaterialTheme.colorScheme.primary
    val strokeWidth = 5.dp

    LaunchedEffect(key1 = isDrawing) {
        if (!isDrawing && inkBuilder.build().strokes.isNotEmpty()) {
            delay(recognitionDelayMillis)
            modelManager?.let { manager ->
                recognizeInk(manager, inkBuilder.build(), hint) { result ->
                    recognizedText = result
                    onAnswerSubmit(result)
                    // Reset the ink so the next recognized value doesn't include already
                    // recognized characters.
                    paths.clear()
                    inkBuilder = Ink.builder()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
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
                        paths.add(currentPath.value)
                        currentPath.value = Path()
                        inkBuilder.addStroke(currentStrokeBuilder.build())
                        currentStrokeBuilder = Stroke.builder()
                        currentPathPoints = emptyList()
                    }
                )
            }
            .clipToBounds()
    ) {
        Canvas(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        ) {
            paths.forEach { path ->
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
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
                    color = strokeColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Composable
fun ExerciseScreen(exercise: SolvableExercise,
                   modelManager: ModelManager?,
                   viewModel: ExerciseBookViewModel,
                   onAnswerSubmit: (String) -> Unit,
                   onAllExercisesComplete: () -> Unit) {

    val backgroundAll: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.paper_top)

    // Observe the answer result state
    val answerResult by viewModel.answerResult.collectAsState()
    var showResultImage by remember { mutableStateOf(false) }
    var resultImageRes by remember { mutableStateOf<Int?>(null) }
    val debug by viewModel.showDebug.collectAsState()

    val catDuration = if (debug) {
        AppMotion.debugDuration
    } else {
        AppMotion.longDuration
    }


    val imageScale by animateFloatAsState(
        targetValue = if (showResultImage) 1.2f else 1f,
        label = "imageScale")
    val alphaScale by animateFloatAsState(
        targetValue = if (showResultImage) 1f else 0f,
        label = "alphaScale")


    LaunchedEffect(answerResult) {
        when (answerResult) {
            is AnswerResult.Correct -> {
                resultImageRes = R.drawable.cat_heart // Replace with your correct image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished(onAllExercisesComplete)
            }
            is AnswerResult.Incorrect -> {
                resultImageRes = R.drawable.cat_cry // Replace with your incorrect image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished(onAllExercisesComplete)
            }
            is AnswerResult.Unrecognized -> {
                resultImageRes = R.drawable.cat_big_eyes // Replace with your incorrect image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished(onAllExercisesComplete)
            }
            is AnswerResult.None -> {
                resultImageRes = null
                showResultImage = false
            }
        }
    }

    Column(
        modifier = Modifier
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
        // Animated content for the exercise question text
        AnimatedContent(
            targetState = exercise.exercise.question(), // Animate when the exercise question changes
            transitionSpec = {
                // Fade in the new text and fade out the old text
                val duration = if (debug) {
                    AppMotion.debugDuration
                } else {
                    AppMotion.mediumDuration
                }
                fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
            }
        ) { targetText -> // The target state (new exercise question)
            Text(
                text = targetText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(312.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 96.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // box for input here
        InkRecognitionBox(Modifier, modelManager, exercise.exercise.getExpectedResult().toString(), onAnswerSubmit)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recognized Text: ???", // $recognizedText",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )

        // Animated visibility for the result image
        AnimatedVisibility(
            visible = showResultImage && resultImageRes != null,
            enter = fadeIn(), // Fade in the image
            exit = fadeOut() // Fade out the image
        ) {
            // Load the image resource
            val imageBitmap = resultImageRes?.let { ImageBitmap.imageResource(id = it) }

            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null, // Provide a proper content description
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally) // Center the image
                        .size(100.dp) // Set the size of the image
                        .graphicsLayer {
                            // Animate scale and alpha for zoom and fade
                            scaleX = imageScale
                            scaleY = imageScale
                            alpha = alphaScale
                        }
                )
            }
        }
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
fun PreviewExerciseScreen() {
    val exercise: Exercise = Subtraction(14, 2)
    val solvableExercise = SolvableExercise(exercise, exercise.getExpectedResult())
    val viewModel : ExerciseBookViewModel = viewModel()
    viewModel.startExercises(ExerciseConfig("subtraction", 12))

    AppTheme {
        ExerciseScreen(solvableExercise, null, viewModel, {}, {})
    }
}

private fun recognizeInk(
    modelManager: ModelManager,
    ink: Ink,
    hint: String,
    onResult: (String) -> Unit
) {
    if (modelManager.recognizer == null) {
        Log.e("InkRecognition", "Recognizer not set")
        return
    }

    modelManager.recognizer!!.recognize(
        ink,
        RecognitionContext.builder().setPreContext("1234").build()
    )
        .addOnSuccessListener { result ->
            val recognizedText = result.candidates.firstOrNull { it.text == hint }?.text
                ?: result.candidates.firstOrNull()?.text
                ?: ""
            onResult(recognizedText)
        }
        .addOnFailureListener { e: Exception ->
            Log.e("InkRecognition", "Error during recognition", e)
            onResult("")
        }
}