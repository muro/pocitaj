package com.codinglikeapirate.pocitaj.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.InkModelManager
import com.codinglikeapirate.pocitaj.data.ExerciseConfig
import com.codinglikeapirate.pocitaj.data.ExerciseSource
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Which screen are we currently on:
sealed class UiState {
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

// One-time navigation events
sealed class NavigationEvent {
    data class NavigateToExercise(val type: String) : NavigationEvent()
    data object NavigateToSummary : NavigationEvent()
    data object NavigateBackToHome : NavigationEvent()
}

class ExerciseViewModel(
    private val inkModelManager: InkModelManager,
    private val exerciseSource: ExerciseSource
) : ViewModel() {
    companion object {
        const val DEBUG_TAP_THRESHOLD = 5
    }

    private var currentExercise: Exercise? = null
    private var exercisesRemaining: Int = 0
    private val exerciseHistory = mutableListOf<Exercise>()

    private val _uiState = MutableStateFlow<UiState>(UiState.ExerciseSetup)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _answerResult = MutableStateFlow<AnswerResult>(AnswerResult.None)
    val answerResult: StateFlow<AnswerResult> = _answerResult.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private val _showDebug = MutableStateFlow(false)
    val showDebug: StateFlow<Boolean> = _showDebug.asStateFlow()
    private var tapCount = 0

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        viewModelScope.launch {
            exerciseSource.initialize(exerciseConfig)
            exerciseHistory.clear()
            exercisesRemaining = exerciseConfig.count
            advanceToNextExercise()
            if (currentExercise != null) {
                _navigationEvents.emit(NavigationEvent.NavigateToExercise(exerciseConfig.operation.name))
            }
        }
    }

    fun recognizeInk(ink: Ink, hint: String) {
        viewModelScope.launch {
            _recognizedText.value = null
            try {
                val result = inkModelManager.recognizeInk(ink, hint)
                _recognizedText.value = result
            } finally {
            }
        }
    }

    private suspend fun advanceToNextExercise() {
        if (exercisesRemaining > 0) {
            currentExercise = exerciseSource.getNextExercise()
            if (currentExercise != null) {
                exercisesRemaining--
            }
        } else {
            currentExercise = null
        }

        if (currentExercise != null) {
            _uiState.value = UiState.ExerciseScreen(currentExercise!!)
        } else {
            // All exercises completed, calculate results and transition
            val results = resultsList()
            _uiState.value = UiState.SummaryScreen(results)
            _navigationEvents.emit(NavigationEvent.NavigateToSummary)
        }
    }



    fun checkAnswer(answer: String, elapsedMs: Int) {
        viewModelScope.launch {
            currentExercise?.let { exercise ->
                val intAnswer = answer.toIntOrNull()
                val isCorrect = intAnswer?.let { exercise.solve(it, elapsedMs) } ?: false

                // Always record the attempt and add to history
                exerciseHistory.add(exercise)
                if (intAnswer != null) {
                    exerciseSource.recordAttempt(exercise, intAnswer, elapsedMs.toLong())
                }

                // Set the UI state based on the result
                _answerResult.value = when {
                    intAnswer == null -> AnswerResult.Unrecognized
                    isCorrect -> AnswerResult.Correct
                    else -> AnswerResult.Incorrect
                }
            }
        }
    }

    fun onResultAnimationFinished() {
        viewModelScope.launch {
            // If the answer was unrecognized, don't advance to the next question.
            // Just reset the state and let the user try again.
            if (_answerResult.value != AnswerResult.Unrecognized) {
                advanceToNextExercise()
            }
            _answerResult.value = AnswerResult.None // Reset answer result state
            _recognizedText.value = null // Reset recognition state
        }
    }

    fun resultsList(): List<ResultDescription> {
        return exerciseHistory.map { exercise ->
            ResultDescription(
                exercise.equationString(),
                ResultStatus.fromBooleanPair(
                    exercise.solved,
                    exercise.correct()
                ),
                exercise.timeTakenMillis ?: 0
            )
        }
    }

    fun onSummaryDone() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateBackToHome)
        }
    }

    fun onSecretAreaTapped() {
        tapCount++
        if (tapCount >= DEBUG_TAP_THRESHOLD) {
            _showDebug.value = true
        }
    }
}

object ExerciseViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
        return ExerciseViewModel(
            inkModelManager = application.inkModelManager,
            exerciseSource = application.exerciseSource
        ) as T
    }
}
