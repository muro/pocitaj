package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


// Which screen are we currently on:
sealed class UiState {
    data class LoadingModel(val errorMessage: String? = null) : UiState()
    data object ExerciseSetup : UiState()
    data class ExerciseScreen(val currentExercise: Exercise) : UiState()
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

class ExerciseBookViewModel(private val inkModelManager: InkModelManager) : ViewModel() {
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

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    fun downloadModel(
        languageCode: String,
        navController: NavHostController
    ) {
        inkModelManager.setModel(languageCode)
        inkModelManager.download().addOnSuccessListener {
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

    fun recognizeInk(ink: Ink, hint: String) {
        viewModelScope.launch {
            _isRecognizing.value = true
            _recognizedText.value = null
            try {
                val result = inkModelManager.recognizeInk(ink, hint)
                _recognizedText.value = result
            } finally {
                _isRecognizing.value = false
            }
        }
    }

    fun deleteActiveModel(languageCode: String) {
        inkModelManager.setModel(languageCode)
        inkModelManager.deleteActiveModel().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model deleted")
        }
    }

    // Function to handle exercise setup completion
    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        if (exerciseConfig.type == ExerciseType.ADDITION.id) {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.ADDITION, exerciseConfig.upTo)
            }
        } else if (exerciseConfig.type == ExerciseType.MISSING_ADDEND.id) {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.MISSING_ADDEND, exerciseConfig.upTo)
            }
        } else if (exerciseConfig.type == ExerciseType.SUBTRACTION.id) {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.SUBTRACTION, exerciseConfig.upTo)
            }
        } else if (exerciseConfig.type == ExerciseType.MISSING_SUBTRAHEND.id) {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.MISSING_SUBTRAHEND, exerciseConfig.upTo)
            }
        } else if (exerciseConfig.type == ExerciseType.MULTIPLICATION.id) {
            _exerciseBook.value.clear()
            for (i in 1..exerciseConfig.count) {
                _exerciseBook.value.generate(ExerciseType.MULTIPLICATION, exerciseConfig.upTo)
            }
        }
        _exerciseIndex = 0
        _uiState.value = UiState.ExerciseScreen(currentExercise())
    }

    private fun currentExercise(): Exercise {
        return _exerciseBook.value.historyList[_exerciseIndex]
    }

    fun checkAnswer(answer: String, elapsedMs: Int) {
        answer.toIntOrNull()?.let {
            if (currentExercise().solve(it, elapsedMs)) {
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
        _recognizedText.value = null // Reset recognition state
    }

    private fun resultsList() {
        results.clear()
        for (exercise in _exerciseBook.value.historyList) {
            results.add(
                ResultDescription(
                    exercise.equationString(),
                    ResultStatus.fromBooleanPair(
                        exercise.solved,
                        exercise.correct()
                    ),
                    exercise.timeTakenMillis ?: 0
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modelManager = (application as PocitajApplication).inkModelManager
        val viewModel: ExerciseBookViewModel by viewModels {
            viewModelFactory {
                initializer {
                    ExerciseBookViewModel(modelManager)
                }
            }
        }

        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavigation(navController, viewModel)
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
fun AppNavigation(navController: NavHostController = rememberNavController(),
                  viewModel: ExerciseBookViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    NavHost(
        navController = navController,
        startDestination = Destinations.LOADING_ROUTE
    ) {
        composable(route = Destinations.LOADING_ROUTE) {
            LoadingScreen(UiState.LoadingModel()) {
                viewModel.downloadModel("en-US", navController)
            }
        }
        composable(route = Destinations.HOME_ROUTE) {
            ExerciseSetupScreen(navController, viewModel) {
                viewModel.deleteActiveModel("en-US")
            }
        }
        composable(route = Destinations.EXERCISE_ROUTE) {
            val exerciseState = uiState as? UiState.ExerciseScreen
            if (exerciseState != null) {
                val exercise: Exercise = exerciseState.currentExercise

                ExerciseScreen(
                    exercise, viewModel,
                    onAnswerSubmit = { answer: String, elapsedMs: Int ->
                        viewModel.checkAnswer(answer, elapsedMs)
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
            val config = ExerciseConfig(ExerciseType.ADDITION.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition")
            navController.navigate(Destinations.exerciseDetailRoute(config.type))
        }) {
            Text("Start Addition")
        }

        Button(onClick = {
            val config = ExerciseConfig(ExerciseType.MISSING_ADDEND.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Addition with missing addend")
            navController.navigate(Destinations.exerciseDetailRoute(config.type))
        }) {
            Text("Start with missing addend")
        }

        Button(onClick = {
            val config = ExerciseConfig(ExerciseType.SUBTRACTION.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Subtraction")
            navController.navigate(Destinations.exerciseDetailRoute(config.type))
        }) {
            Text("Start Subtraction")
        }

        Button(onClick = {
            val config = ExerciseConfig(ExerciseType.MISSING_SUBTRAHEND.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Subtraction with missing subtrahend")
            navController.navigate(Destinations.exerciseDetailRoute(config.type))
        }) {
            Text("Start Subtraction with missing subtrahend")
        }

        Button(onClick = {
            val config = ExerciseConfig(ExerciseType.MULTIPLICATION.id, 10, 2) // Replace with actual selection
            exerciseBookViewModel.startExercises(config)
            Log.i("ExerciseBookActivity", "Starting Multiplication")
            navController.navigate(Destinations.exerciseDetailRoute(config.type))
        }) {
            Text("Start Multiplication")
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
fun PreviewExerciseSetupScreen() {
    val viewModel : ExerciseBookViewModel = viewModel()
    val navController = rememberNavController()
    AppTheme {
        ExerciseSetupScreen(navController, viewModel) {}
    }
}
