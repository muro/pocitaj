package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.google.mlkit.vision.digitalink.RecognitionContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.mlkit.vision.digitalink.Ink.Stroke as InkStroke


// Which screen are we currently on:
sealed class UiState {
    data class LoadingModel(val errorMessage: String? = null) : UiState()
    data object ExerciseSetup : UiState()
    data class ExerciseScreen(val currentExercise: ExerciseBook.Exercise) : UiState()
    data class SummaryScreen(val results: String) : UiState()
}

// Are we animating a result ack
sealed class AnswerResult {
    data object Correct : AnswerResult()
    data object Incorrect : AnswerResult()
    data object Unrecognized : AnswerResult()
    data object None : AnswerResult() // Initial state
}

data class ExerciseConfig(val type: String, val level: Int)

class ExerciseBookViewModel : ViewModel() {
    private val _exerciseBook: MutableState<ExerciseBook> = mutableStateOf(ExerciseBook())
    private var _exerciseIndex = 0

    private val _uiState = MutableStateFlow<UiState>(UiState.LoadingModel())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _answerResult = MutableStateFlow<AnswerResult>(AnswerResult.None)
    val answerResult: StateFlow<AnswerResult> = _answerResult.asStateFlow()

    fun downloadModel(modelManager: ModelManager, languageCode: String) {
        modelManager.setModel(languageCode)
        modelManager.download().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model download succeeded")
            _uiState.value = UiState.ExerciseSetup
        }.addOnFailureListener {
            Log.e("ExerciseBookViewModel", "Model download failed", it)
            _uiState.value = UiState.LoadingModel(errorMessage = it.localizedMessage ?: "Unknown error")}
    }

    // Function to handle exercise setup completion
    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        if (exerciseConfig.type == "addition") {
            _exerciseBook.value.clear()
            _exerciseBook.value.generate()
            _exerciseBook.value.generate()
            _exerciseBook.value.generate()
        } else if (exerciseConfig.type == "subtraction") {
            _exerciseBook.value.clear()
            _exerciseBook.value.generate()

        }
        _exerciseIndex = 0
        _uiState.value = UiState.ExerciseScreen(currentExercise())
    }

    private fun currentExercise(): ExerciseBook.Exercise {
        return _exerciseBook.value.historyList[_exerciseIndex]
    }

    fun checkAnswer(answer: String) {
        answer.toIntOrNull()?.let {
            if (currentExercise().solve(it)) {
                _answerResult.value = AnswerResult.Correct
            } else {
                _answerResult.value = AnswerResult.Incorrect
            }
            if (_exerciseIndex < _exerciseBook.value.historyList.size) {
                ++_exerciseIndex
            }
        } ?: run {
            _answerResult.value = AnswerResult.Unrecognized
        }
    }

    fun onResultAnimationFinished() {
        if (_exerciseIndex < _exerciseBook.value.historyList.size) {
            _uiState.value = UiState.ExerciseScreen(currentExercise())
        } else {
            // All exercises completed, calculate results and transition
            val results = "You finished all exercises!" // Calculate actual results
            _uiState.value = UiState.SummaryScreen(results)
        }
        _answerResult.value = AnswerResult.None // Reset answer result state
    }

    init {
        // initialize with 2 exercises:
        _exerciseBook.value.generate()
        _exerciseBook.value.generate()
    }
}

class ExerciseBookActivity : ComponentActivity() {

    @JvmField
    @VisibleForTesting
    var modelManager = ModelManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ExerciseBookViewModel by viewModels()
        modelManager.setModel("en-US")

        setContent {
            AppTheme {
                ExerciseScreen(modelManager, viewModel)
            }
        }
    }
}

@Composable
fun ExerciseScreen(
    modelManager: ModelManager? = null,
    exerciseBookViewModel: ExerciseBookViewModel
) {
    val uiState by exerciseBookViewModel.uiState.collectAsState()

    // Trigger model download when the composable is first composed
    LaunchedEffect(Unit) {
        // You might want to check if the model is already downloaded here
        // before starting the download process.
        modelManager?.let {
            exerciseBookViewModel.downloadModel(it, "en-US") // Start download
        }
    }

    when (uiState) {
        is UiState.LoadingModel -> {
            LoadingScreen(uiState as UiState.LoadingModel)
            {
                modelManager?.let {
                    exerciseBookViewModel.downloadModel(it, "en-US") // Start download
                }
            }
        }
        is UiState.ExerciseSetup -> {
            ExerciseSetupScreen(
                onStartExercises = { config ->
                    exerciseBookViewModel.startExercises(config)
                }
            ) // Display exercise setup screen
        }
        is UiState.ExerciseScreen -> {
            val currentExercise = (uiState as UiState.ExerciseScreen).currentExercise
            // Display the current exercise UI
            ExerciseComposable(
                exercise = currentExercise,
                modelManager = modelManager,
                exerciseBookViewModel = exerciseBookViewModel,
                onAnswerSubmit = { answer ->
                    exerciseBookViewModel.checkAnswer(answer)
            })
        }
        is UiState.SummaryScreen -> {
            val results = (uiState as UiState.SummaryScreen).results
            // Display the summary UI
            SummaryComposable(results = results)
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
fun ExerciseScreenPreview() {
    // val modelManager = ModelManager()
    AppTheme {
        ExerciseScreen(null, viewModel())
    }
}

@Composable
fun LoadingScreen(state: UiState.LoadingModel, retry: () -> Unit) {
    // UI for the loading screen (e.g., progress indicator, text)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.errorMessage == null) {
            // Display loading indicator and message
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Downloading model...")
        } else {
            // Display error message and potentially a retry button
            Text("Error downloading model:", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
            // Add a retry button that calls ViewModel to retry download
            Button(onClick = { retry() }) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ExerciseSetupScreen(onStartExercises: (ExerciseConfig) -> Unit) {
    // UI for choosing exercise type and starting exercises
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose Exercise Type")
        Spacer(modifier = Modifier.height(16.dp))
        // Add UI elements (e.g., Radio buttons, dropdowns) for selecting exercise type and level
        Button(onClick = {
            // Get selected exercise configuration
            val config = ExerciseConfig("Math", 1) // Replace with actual selection
            onStartExercises(config) // Call ViewModel to start exercises
        }) {
            Text("Start Exercises")
        }
    }
}


@Composable
fun InkRecognitionBox(
    modifier: Modifier = Modifier,
    modelManager: ModelManager? = null,
    onAnswerSubmit: (String) -> Unit
) {
    val recognitionDelayMillis = 1000L

    var recognizedText by remember { mutableStateOf("") }
    val currentPath = remember { mutableStateOf(Path()) }
    val paths = remember { mutableStateListOf<Path>() }
    var currentStrokeBuilder = remember { InkStroke.builder() }
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
                recognizeInk(manager, inkBuilder.build()) { result ->
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
                        currentStrokeBuilder = InkStroke.builder()
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
                    style = Stroke(
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
                    style = Stroke(
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
fun ExerciseComposable(exercise: ExerciseBook.Exercise,
                       modelManager: ModelManager? = null,
                       exerciseBookViewModel: ExerciseBookViewModel,
                       onAnswerSubmit: (String) -> Unit) {

    val backgroundAll: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.paper_top)

    // Observe the answer result state
    val answerResult by exerciseBookViewModel.answerResult.collectAsState()
    var showResultImage by remember { mutableStateOf(false) }
    var resultImageRes by remember { mutableStateOf<Int?>(null) }

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
                delay(timeMillis = AppMotion.longDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                exerciseBookViewModel.onResultAnimationFinished()
            }
            is AnswerResult.Incorrect -> {
                resultImageRes = R.drawable.cat_cry // Replace with your incorrect image resource
                showResultImage = true
                delay(AppMotion.longDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                exerciseBookViewModel.onResultAnimationFinished()
            }
            is AnswerResult.Unrecognized -> {
                resultImageRes = R.drawable.cat_big_eyes // Replace with your incorrect image resource
                showResultImage = true
                delay(AppMotion.mediumDuration.toLong()) // Display for 500 milliseconds
                showResultImage = false
                // Call ViewModel function to signal animation is finished
                exerciseBookViewModel.onResultAnimationFinished()
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
            targetState = exercise.question(), // Animate when the exercise question changes
            transitionSpec = {
                // Fade in the new text and fade out the old text
                fadeIn(animationSpec = tween(AppMotion.mediumDuration)) togetherWith fadeOut(animationSpec = tween(AppMotion.mediumDuration))
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
        InkRecognitionBox(Modifier, modelManager, onAnswerSubmit)

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
@Composable
fun ExerciseComposablePreview() {
    AppTheme {
        ExerciseComposable(ExerciseBook.Addition(1, 2), null, viewModel()) {}
    }
}

@Composable
fun SummaryComposable(results: String) {
    // UI to display the summary of results
    Column {
        Text(text = "Summary:")
        Text(text = results)
        // Optionally, add a button to restart exercises or navigate elsewhere
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Composable
fun SummaryComposablePreview() {
    AppTheme {
        SummaryComposable("results")
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