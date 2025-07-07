package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codinglikeapirate.pocitaj.logic.Equation
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.Subtraction
import com.codinglikeapirate.pocitaj.ui.theme.AppMotion
import com.codinglikeapirate.pocitaj.ui.theme.AppTheme
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import com.google.mlkit.vision.digitalink.Ink.Stroke
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InkRecognitionBox(
    modifier: Modifier = Modifier,
    viewModel: ExerciseBookViewModel,
    hint: String
) {
    val recognitionDelayMillis = 1_000L

    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var currentStrokeBuilder = remember { Stroke.builder() }
    var inkBuilder by remember { mutableStateOf(Ink.builder()) }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }

    val scope = rememberCoroutineScope()
    var recognitionJob by remember { mutableStateOf<Job?>(null) }

    val backgroundAnswer: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.paper_answer)

    val strokeColor = MaterialTheme.colorScheme.errorContainer
    val activeStrokeColor = MaterialTheme.colorScheme.error
    val strokeWidth = 5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("InkCanvas")
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
                        recognitionJob?.cancel() // Cancel any pending recognition
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
                        paths.add(currentPath.value)
                        currentPath.value = Path()
                        inkBuilder.addStroke(currentStrokeBuilder.build())
                        currentStrokeBuilder = Stroke.builder()
                        currentPathPoints = emptyList()
                        recognitionJob = scope.launch { // Launch a new recognition job
                            delay(recognitionDelayMillis)
                            val ink = inkBuilder.build()
                            paths.clear()
                            inkBuilder = Ink.builder()
                            viewModel.recognizeInk(ink, hint)
                        }
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
                    color = activeStrokeColor,
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
fun ExerciseScreen(exercise: Exercise,
                   viewModel: ExerciseBookViewModel,
                   onAnswerSubmit: (String, Int) -> Unit) {

    val backgroundAll: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.paper_top)

    // Observe the answer result state
    val answerResult by viewModel.answerResult.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    var showResultImage by remember { mutableStateOf(false) }
    var resultImageRes by remember { mutableStateOf<Int?>(null) }
    val debug by viewModel.showDebug.collectAsState()

    var elapsedTimeMillis by remember { mutableIntStateOf(0) }

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

    // LaunchedEffect to start the timer when the screen is visible
    LaunchedEffect(exercise) {
        val startTime = System.currentTimeMillis()
        while (true) {
            elapsedTimeMillis = (System.currentTimeMillis() - startTime).toInt()
            delay(100) // Update every 100 milliseconds
        }
    }

    LaunchedEffect(recognizedText) {
        recognizedText?.let {
            val finalElapsedTime = elapsedTimeMillis
            onAnswerSubmit(it, finalElapsedTime)
        }
    }

    LaunchedEffect(answerResult) {
        when (answerResult) {
            is AnswerResult.Correct -> {
                resultImageRes = R.drawable.cat_heart // Replace with your correct image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished()
            }
            is AnswerResult.Incorrect -> {
                resultImageRes = R.drawable.cat_cry // Replace with your incorrect image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished()
            }
            is AnswerResult.Unrecognized -> {
                resultImageRes = R.drawable.cat_big_eyes // Replace with your incorrect image resource
                showResultImage = true
                delay(timeMillis = catDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                viewModel.onResultAnimationFinished()
            }
            is AnswerResult.None -> {
                resultImageRes = null
                showResultImage = false
            }
        }
    }

    Box(
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated content for the exercise question text
            AnimatedContent(
                targetState = exercise.equation.question(), // Animate when the exercise question changes
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
            InkRecognitionBox(
                Modifier,
                viewModel,
                exercise.equation.getExpectedResult().toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recognized Text: ${recognizedText ?: "..."}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )

            Text(
                text = "Time: ${elapsedTimeMillis / 1000}s ${elapsedTimeMillis % 1000}ms",
                modifier = Modifier
                    // .align(Alignment.TopEnd) // Align to top-right
                    .padding(8.dp), // Add some padding
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        // Animated visibility for the result image
        AnimatedVisibility(
            visible = showResultImage && resultImageRes != null,
            enter = fadeIn(), // Fade in the image
            exit = fadeOut(), // Fade out the image
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Load the image resource
            val imageBitmap = resultImageRes?.let { ImageBitmap.imageResource(id = it) }

            val contentDesc = when (answerResult) {
                is AnswerResult.Correct -> "Correct Answer Image"
                is AnswerResult.Incorrect -> "Incorrect Answer Image"
                is AnswerResult.Unrecognized -> "Unrecognized Answer Image"
                else -> null
            }
            if (imageBitmap != null && contentDesc != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = contentDesc,
                    modifier = Modifier
                        .size(200.dp) // Set the size of the image
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
    val equation: Equation = Subtraction(14, 2)
    val exercise = Exercise(equation, equation.getExpectedResult())
    val viewModel : ExerciseBookViewModel = viewModel()
    viewModel.startExercises(ExerciseConfig("subtraction", 12))

    AppTheme {
        ExerciseScreen(exercise, viewModel) {_: String, _: Int -> }
    }
}

