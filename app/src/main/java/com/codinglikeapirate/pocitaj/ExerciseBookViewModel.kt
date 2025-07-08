package com.codinglikeapirate.pocitaj

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinglikeapirate.pocitaj.data.ExerciseRepository
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

// One-time navigation events
sealed class NavigationEvent {
    data object NavigateToHome : NavigationEvent()
    data class NavigateToExercise(val type: String) : NavigationEvent()
    data object NavigateToSummary : NavigationEvent()
    data object NavigateBackToHome : NavigationEvent()
}


class ExerciseBookViewModel(
    private val inkModelManager: InkModelManager,
    private val exerciseBook: ExerciseBook,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    companion object {
        const val DEBUG_TAP_THRESHOLD = 5
        private const val DEFAULT_USER_ID = 1L // Placeholder for user management
    }

    private var currentExercise: Exercise? = null
    private var exercisesRemaining: Int = 0

    private val _uiState = MutableStateFlow<UiState>(UiState.LoadingModel())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _answerResult = MutableStateFlow<AnswerResult>(AnswerResult.None)
    val answerResult: StateFlow<AnswerResult> = _answerResult.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private val _showDebug = MutableStateFlow(false)
    val showDebug: StateFlow<Boolean> = _showDebug.asStateFlow()
    private var tapCount = 0

    private val results = ArrayList<ResultDescription>()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    fun downloadModel(languageCode: String) {
        inkModelManager.setModel(languageCode)
        inkModelManager.download().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model download succeeded")
            _uiState.value = UiState.ExerciseSetup
            viewModelScope.launch {
                _navigationEvents.emit(NavigationEvent.NavigateToHome)
            }
        }.addOnFailureListener {
            Log.e("ExerciseBookViewModel", "Model download failed", it)
            _uiState.value =
                UiState.LoadingModel(errorMessage = it.localizedMessage ?: "Unknown error")
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

    // Function to handle exercise setup completion
    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        viewModelScope.launch {
            exercisesRemaining = exerciseConfig.count
            exerciseBook.generateExercises(exerciseConfig)
            advanceToNextExercise()
            if (currentExercise != null) {
                _navigationEvents.emit(NavigationEvent.NavigateToExercise(exerciseConfig.type))
            }
        }
    }

    private suspend fun advanceToNextExercise() {
        if (exercisesRemaining > 0) {
            currentExercise = exerciseBook.getNextExercise()
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
            resultsList()
            _uiState.value = UiState.SummaryScreen(results)
            _navigationEvents.emit(NavigationEvent.NavigateToSummary)
        }
    }



    fun checkAnswer(answer: String, elapsedMs: Int) {
        viewModelScope.launch {
            currentExercise?.let { exercise ->
                answer.toIntOrNull()?.let { intAnswer ->
                    val isCorrect = exercise.solve(intAnswer, elapsedMs)
                    exerciseRepository.recordAttempt(DEFAULT_USER_ID, exercise, intAnswer, elapsedMs.toLong())
                    if (isCorrect) {
                        _answerResult.value = AnswerResult.Correct
                    } else {
                        _answerResult.value = AnswerResult.Incorrect
                    }
                } ?: run {
                    _answerResult.value = AnswerResult.Unrecognized
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

    private fun resultsList() {
        results.clear()
        for (exercise in exerciseBook.historyList) {
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