package dev.aidistillery.pocitaj.ui.exercise

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.Ink.Point
import com.google.mlkit.vision.digitalink.Ink.Stroke
import dev.aidistillery.pocitaj.InkModelManager
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Equation
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.Subtraction
import dev.aidistillery.pocitaj.ui.components.AutoSizeText
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import dev.aidistillery.pocitaj.ui.theme.customColors
import dev.aidistillery.pocitaj.ui.theme.motion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InkRecognitionBox(
    modifier: Modifier = Modifier,
    viewModel: ExerciseViewModel,
    hint: String,
    onDragStart: () -> Unit
) {
    val recognitionDelayMillis = 1_000L

    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var currentStrokeBuilder = remember { Stroke.builder() }
    var inkBuilder by remember { mutableStateOf(Ink.builder()) }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }

    val scope = rememberCoroutineScope()
    var recognitionJob by remember { mutableStateOf<Job?>(null) }

    val strokeColor = MaterialTheme.colorScheme.errorContainer
    val activeStrokeColor = MaterialTheme.colorScheme.error
    val strokeWidth = 5.dp

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.customColors.paperGradientStart,
            MaterialTheme.customColors.paperGradientEnd
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("InkCanvas")
            .background(backgroundBrush, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart()
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
fun ExerciseScreen(
    exercise: Exercise,
    viewModel: ExerciseViewModel,
    debugMode: Boolean,
    onAnswerSubmit: (String, Int) -> Unit
) {
    BackHandler {
        viewModel.endSessionEarly()
    }

    PocitajScreen {
        val answerResult by viewModel.answerResult.collectAsState()
        val recognizedText by viewModel.recognizedText.collectAsState()
        var showResultImage by remember { mutableStateOf(false) }

        var elapsedTimeMillis by remember { mutableIntStateOf(0) }
        var finalElapsedTimeMillis by remember { mutableIntStateOf(0) }

        val catDuration = if (debugMode) {
            MaterialTheme.motion.debug
        } else {
            MaterialTheme.motion.long
        }

        val transitionDuration = if (debugMode) {
            MaterialTheme.motion.debug
        } else {
            MaterialTheme.motion.medium
        }

        // LaunchedEffect to start the timer when the screen is visible
        LaunchedEffect(exercise) {
            finalElapsedTimeMillis = 0
            val startTime = System.currentTimeMillis()
            while (true) {
                elapsedTimeMillis = (System.currentTimeMillis() - startTime).toInt()
                delay(100) // Update every 100 milliseconds
            }
        }

        LaunchedEffect(recognizedText) {
            recognizedText?.let {
                onAnswerSubmit(it, finalElapsedTimeMillis)
            }
        }

        LaunchedEffect(answerResult) {
            if (answerResult !is AnswerResult.None) {
                showResultImage = true
                val delayMillis = when (answerResult) {
                    is AnswerResult.ShowCorrection -> catDuration * 2
                    else -> catDuration
                }
                delay(timeMillis = delayMillis.toLong())
                showResultImage = false
                viewModel.onFeedbackAnimationFinished()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Animated content for the exercise question text
                AnimatedContent(
                    // This correctly doesn't animate when the answer was unrecognized.
                    targetState = exercise, // Animate when the exercise object changes
                    transitionSpec = {
                        // Vertical slide + fade for normal mode
                        (slideInVertically(animationSpec = tween(transitionDuration)) { height -> height } + fadeIn(animationSpec = tween(transitionDuration))) togetherWith
                                (slideOutVertically(animationSpec = tween(transitionDuration)) { height -> -height } + fadeOut(animationSpec = tween(transitionDuration)))
                    }
                ) { targetExercise -> // The target state (new exercise object)
                    AutoSizeText(
                        text = targetExercise.equation.question(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(312.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // box for input here
                InkRecognitionBox(
                    Modifier,
                    viewModel,
                    exercise.equation.getExpectedResult().toString()
                ) {
                    if (finalElapsedTimeMillis == 0) {
                        finalElapsedTimeMillis = elapsedTimeMillis
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (debugMode) {
                    Text(
                        text = "Recognized Text: ${recognizedText ?: "..."}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Time: ${elapsedTimeMillis / 1000}s ${elapsedTimeMillis % 1000}ms",
                        modifier = Modifier
                            // .align(Alignment.TopEnd) // Align to top-right
                            .padding(8.dp), // Add some padding
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val workingSet by viewModel.workingSet.collectAsState()
                    if (workingSet.isNotEmpty()) {
                        Text(
                            text = "Working Set:\n${workingSet.joinToString("\n")}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            ResultDisplay(
                answerResult = answerResult,
                showResultImage = showResultImage,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
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
    val mockInkModelManager = object : InkModelManager {
        override fun setModel(languageTag: String): String = "Model set"
        override fun deleteActiveModel(): com.google.android.gms.tasks.Task<String?> =
            Tasks.forResult(null)

        override fun download(): com.google.android.gms.tasks.Task<String?> = Tasks.forResult(null)
        override suspend fun recognizeInk(ink: Ink, hint: String): String = "12"
    }
    val mockExerciseSource = object : ExerciseSource {
        override suspend fun initialize(config: ExerciseConfig) {}
        override fun getNextExercise(): Exercise? = null
        override suspend fun recordAttempt(
            exercise: Exercise,
            submittedAnswer: Int,
            durationMs: Long
        ) {
        }
    }
    val viewModel = ExerciseViewModel(mockInkModelManager, mockExerciseSource)
    viewModel.startExercises(ExerciseConfig(Operation.SUBTRACTION, 12, 10))

    AppTheme {
        ExerciseScreen(exercise, viewModel, false) { _: String, _: Int -> }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
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
fun PreviewExerciseScreenDebug() {
    val equation: Equation = Subtraction(14, 2)
    val exercise = Exercise(equation, equation.getExpectedResult())
    val mockInkModelManager = object : InkModelManager {
        override fun setModel(languageTag: String): String = "Model set"
        override fun deleteActiveModel(): com.google.android.gms.tasks.Task<String?> =
            Tasks.forResult(null)

        override fun download(): com.google.android.gms.tasks.Task<String?> = Tasks.forResult(null)
        override suspend fun recognizeInk(ink: Ink, hint: String): String = "12"
    }
    val mockExerciseSource = object : ExerciseSource {
        override suspend fun initialize(config: ExerciseConfig) {}
        override fun getNextExercise(): Exercise? = null
        override suspend fun recordAttempt(
            exercise: Exercise,
            submittedAnswer: Int,
            durationMs: Long
        ) {
        }
    }
    val viewModel = ExerciseViewModel(mockInkModelManager, mockExerciseSource)
    viewModel.startExercises(ExerciseConfig(Operation.SUBTRACTION, 12, 10))

    AppTheme {
        ExerciseScreen(exercise, viewModel, true) { _: String, _: Int -> }
    }
}
