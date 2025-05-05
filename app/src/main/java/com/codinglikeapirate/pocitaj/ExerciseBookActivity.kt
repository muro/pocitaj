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
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    data class SummaryScreen(val results: List<ResultDescription>) : UiState()
}

// Are we animating a result ack
sealed class AnswerResult {
    data object Correct : AnswerResult()
    data object Incorrect : AnswerResult()
    data object Unrecognized : AnswerResult()
    data object None : AnswerResult() // Initial state
}

data class ExerciseConfig(val type: String, val upTo: Int = 10, val count: Int = 10)

class ExerciseBookViewModel : ViewModel() {
    companion object {
        const val DEBUG_TAP_THRESHOLD = 5
    }

    private val _exerciseBook: MutableState<ExerciseBook> = mutableStateOf(ExerciseBook())
    private var _exerciseIndex = 0

    private val _uiState = MutableStateFlow<UiState>(UiState.LoadingModel())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _answerResult = MutableStateFlow<AnswerResult>(AnswerResult.None)
    val answerResult: StateFlow<AnswerResult> = _answerResult.asStateFlow()

    private val _showDebug = MutableStateFlow(false)
    val showDebug: StateFlow<Boolean> = _showDebug.asStateFlow()
    private var tapCount = 0

    private val results = ArrayList<ResultDescription>()

    fun downloadModel(
        modelManager: ModelManager,
        languageCode: String,
        navController: NavHostController
    ) {
        modelManager.setModel(languageCode)
        modelManager.download().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model download succeeded")
            _uiState.value = UiState.ExerciseSetup
            navController.navigate(Destinations.HOME_ROUTE) {
                popUpTo(Destinations.LOADING_ROUTE) {
                    inclusive = true // Remove the "loading" destination itself
                }
            }
        }.addOnFailureListener {
            Log.e("ExerciseBookViewModel", "Model download failed", it)
            _uiState.value =
                UiState.LoadingModel(errorMessage = it.localizedMessage ?: "Unknown error")
        }
    }

    fun deleteActiveModel(modelManager: ModelManager, languageCode: String) {
        modelManager.setModel(languageCode)
        modelManager.deleteActiveModel().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model deleted")
        }
    }

    // Function to handle exercise setup completion
    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        if (exerciseConfig.type == "addition") {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.ADDITION, exerciseConfig.upTo)
            }
        } else if (exerciseConfig.type == "subtraction") {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.SUBTRACTION, exerciseConfig.upTo)
            }
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

    fun onResultAnimationFinished(onAllExercisesComplete: () -> Unit) {
        if (_exerciseIndex < _exerciseBook.value.historyList.size) {
            _uiState.value = UiState.ExerciseScreen(currentExercise())
        } else {
            // All exercises completed, calculate results and transition
            resultsList()
            _uiState.value = UiState.SummaryScreen(results)
            onAllExercisesComplete()
        }
        _answerResult.value = AnswerResult.None // Reset answer result state
    }

    private fun resultsList() {
        results.clear()
        for (exercise in _exerciseBook.value.historyList) {
            results.add(
                ResultDescription(
                    exercise.equation(),
                    ResultStatus.fromBooleanPair(
                        exercise.solved(),
                        exercise.correct()
                    )
                )
            )
        }
    }

    fun onSecretAreaTapped() {
        tapCount++
        if (tapCount >= DEBUG_TAP_THRESHOLD) {
            _showDebug.value = true
        }
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
                val navController = rememberNavController()
                AppNavigation(modelManager, navController, viewModel)
            }
        }
    }
}

object Destinations {
    const val LOADING_ROUTE = "loading"
    const val HOME_ROUTE = "home"
    const val EXERCISE_ROUTE = "exercise/{type}"
    const val SUMMARY_ROUTE = "summary"
    fun exerciseDetailRoute(type: String) = "exercise/$type"
}

@Composable
fun AppNavigation(modelManager: ModelManager,
                  navController: NavHostController = rememberNavController(),
                  viewModel: ExerciseBookViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    NavHost(
        navController = navController,
        startDestination = Destinations.LOADING_ROUTE
    ) {
        composable(route = Destinations.LOADING_ROUTE) {
            LoadingScreen(UiState.LoadingModel()) {
                viewModel.downloadModel(modelManager, "en-US", navController)
            }
        }
        composable(route = Destinations.HOME_ROUTE) {
            ExerciseSetupScreen(navController, viewModel) {
                viewModel.deleteActiveModel(modelManager, "en-US")
            }
        }
        composable(route = Destinations.EXERCISE_ROUTE) {
            val exerciseState = uiState as? UiState.ExerciseScreen
            if (exerciseState != null) {
                val exercise: ExerciseBook.Exercise = exerciseState.currentExercise

                ExerciseScreen(
                    exercise, modelManager, viewModel,
                    onAnswerSubmit = { answer ->
                        viewModel.checkAnswer(answer)
                    },
                    onAllExercisesComplete = {
                        navController.navigate(Destinations.SUMMARY_ROUTE) {
                            popUpTo(Destinations.HOME_ROUTE) { inclusive = false }
                        }
                    })
            }
        }
        composable(route = Destinations.SUMMARY_ROUTE) {
            val summaryState = uiState as? UiState.SummaryScreen
            if (summaryState != null) {
                ResultsScreen(summaryState.results) {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(state: UiState.LoadingModel, downloadModel: () -> Unit) {
    LaunchedEffect(Unit) {
        downloadModel()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.errorMessage == null) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Downloading model...")
        } else {
            Text("Error downloading model:", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Please close the app and restart while an internet connection is available.")
        }
    }
}

@Composable
fun ExerciseSetupScreen(navController: NavHostController,
                         exerciseBookViewModel: ExerciseBookViewModel,
                         onModelDelete: () -> Unit) {
    val showDebug by exerciseBookViewModel.showDebug.collectAsState()

    // UI for choosing exercise type and starting exercises
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose Exercise Type", modifier = Modifier.clickable {
            exerciseBookViewModel.onSecretAreaTapped()
        })
        Spacer(modifier = Modifier.height(16.dp))
        // Add UI elements (e.g., Radio buttons, dropdowns) for selecting exercise type and level
        Button(onClick = {
            // Get selected exercise configuration
            val config = ExerciseConfig("addition", 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition")
            navController.navigate(Destinations.exerciseDetailRoute("addition"))
        }) {
            Text("Start Addition")
        }

        Button(onClick = {
            // Get selected exercise configuration
            val config = ExerciseConfig("subtraction", 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition")
            navController.navigate(Destinations.exerciseDetailRoute("subtraction"))
        }) {
            Text("Start Subtraction")
        }

        Spacer(modifier = Modifier.height(64.dp))
        // Leave in for now
        if (showDebug) {
            Button(onClick = { onModelDelete() }) {
                Text("Delete model")
            }
        }
    }
}


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
fun ExerciseScreen(exercise: ExerciseBook.Exercise,
                   modelManager: ModelManager,
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
            targetState = exercise.question(), // Animate when the exercise question changes
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
        InkRecognitionBox(Modifier, modelManager, exercise.getExpectedResult().toString(), onAnswerSubmit)

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
    val exercise: ExerciseBook.Exercise = ExerciseBook.Addition(1, 2)
    val viewModel : ExerciseBookViewModel = viewModel()
    viewModel.startExercises(ExerciseConfig("subtraction", 12))
    val modelManager = ModelManager()
    AppTheme {
        ExerciseScreen(exercise, modelManager, viewModel, {}, {})
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