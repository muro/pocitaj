package com.codinglikeapirate.pocitaj

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


data class ExerciseConfig(val type: String, val upTo: Int = 10, val count: Int = 10)

class ExerciseBookViewModel(private val inkModelManager: InkModelManager) : ViewModel() {
    companion object {
        const val DEBUG_TAP_THRESHOLD = 5
    }

    private var _exerciseBook: MutableState<ExerciseBook> = mutableStateOf(ExerciseBook())
    private var _exerciseIndex = 0

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

    fun deleteActiveModel(languageCode: String) {
        inkModelManager.setModel(languageCode)
        inkModelManager.deleteActiveModel().addOnSuccessListener {
            Log.i("ExerciseBookViewModel", "Model deleted")
        }
    }

    // Function to handle exercise setup completion
    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        _exerciseBook.value.clear()

        val exerciseType: ExerciseType = when (exerciseConfig.type) {
            ExerciseType.ADDITION.id -> ExerciseType.ADDITION
            ExerciseType.MISSING_ADDEND.id -> ExerciseType.MISSING_ADDEND
            ExerciseType.SUBTRACTION.id -> ExerciseType.SUBTRACTION
            ExerciseType.MISSING_SUBTRAHEND.id -> ExerciseType.MISSING_SUBTRAHEND
            ExerciseType.MULTIPLICATION.id -> ExerciseType.MULTIPLICATION
            else -> {
                Log.e("ExerciseBookViewModel", "Unknown exercise type: ${exerciseConfig.type}")
                ExerciseType.ADDITION
            }
        }

        repeat(exerciseConfig.count) {
            _exerciseBook.value.generate(exerciseType, exerciseConfig.upTo)
        }

        _exerciseIndex = 0
        _uiState.value = UiState.ExerciseScreen(currentExercise())
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateToExercise(exerciseConfig.type))
        }
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

    fun onResultAnimationFinished() {
        if (_exerciseIndex < _exerciseBook.value.historyList.size) {
            _uiState.value = UiState.ExerciseScreen(currentExercise())
        } else {
            // All exercises completed, calculate results and transition
            resultsList()
            _uiState.value = UiState.SummaryScreen(results)
            viewModelScope.launch {
                _navigationEvents.emit(NavigationEvent.NavigateToSummary)
            }
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

    @VisibleForTesting
    fun setExerciseBookForTesting(book: ExerciseBook) {
        _exerciseBook.value = book
        _exerciseIndex = 0
        _uiState.value = UiState.ExerciseScreen(currentExercise())
    }
}
