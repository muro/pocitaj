package dev.aidistillery.pocitaj.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.mlkit.vision.digitalink.recognition.Ink
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.BuildConfig
import dev.aidistillery.pocitaj.InkModelManager
import dev.aidistillery.pocitaj.data.AdaptiveExerciseSource
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.logic.Exercise
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

sealed class AnswerResult {
    data object Correct : AnswerResult()
    data object Incorrect : AnswerResult()
    data object Unrecognized : AnswerResult()
    data class ShowCorrection(val equation: String) : AnswerResult()
    data object None : AnswerResult() // Initial state
}

// One-time navigation events
sealed class NavigationEvent {
    data class NavigateToExercise(val type: String) : NavigationEvent()
    data object NavigateToSummary : NavigationEvent()
    data object NavigateBackToHome : NavigationEvent()
}

class ExerciseViewModel(
    private val inkModelManager: InkModelManager, private val exerciseSource: ExerciseSource
) : ViewModel() {
    private var currentExercise: Exercise? = null
    private var exercisesRemaining: Int = 0
    private val exerciseHistory = mutableListOf<Exercise>()
    private var currentLevelId: String? = null
    private var currentExerciseConfig: ExerciseConfig? = null

    private val _uiState = MutableStateFlow<UiState>(UiState.ExerciseSetup)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _answerResult = MutableStateFlow<AnswerResult>(AnswerResult.None)
    val answerResult: StateFlow<AnswerResult> = _answerResult.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    private val _workingSet = MutableStateFlow<List<String>>(emptyList())
    val workingSet: StateFlow<List<String>> = _workingSet.asStateFlow()

    fun startExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        viewModelScope.launch {
            currentExerciseConfig = exerciseConfig
            currentLevelId = exerciseConfig.levelId
            exerciseSource.initialize(exerciseConfig)
            exerciseHistory.clear()
            exercisesRemaining = exerciseConfig.count
            updateWorkingSetForDebug()
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
            handleSessionComplete()
        }
    }


    fun checkAnswer(answer: String, elapsedMs: Int) {
        viewModelScope.launch {
            currentExercise?.let { exercise ->
                val intAnswer = answer.toIntOrNull()

                if (intAnswer != null) {
                    exercise.solve(intAnswer, elapsedMs)
                }

                exerciseHistory.add(exercise.copy())

                if (intAnswer == null) {
                    onUnrecognizedAnswer(exercise, answer, elapsedMs)
                } else {
                    if (exercise.correct()) {
                        onCorrectAnswer(exercise, intAnswer, elapsedMs)
                    } else {
                        onIncorrectAnswer(exercise, intAnswer, elapsedMs)
                    }
                }
                updateWorkingSetForDebug()
            }
        }
    }

    @Suppress("unused")
    private fun onUnrecognizedAnswer(exercise: Exercise, answer: String, elapsedMs: Int) {
        _answerResult.value = AnswerResult.Unrecognized
    }

    private suspend fun onCorrectAnswer(exercise: Exercise, answer: Int, elapsedMs: Int) {
        exerciseSource.recordAttempt(exercise, answer, elapsedMs.toLong())
        _answerResult.value = AnswerResult.Correct
    }

    private suspend fun onIncorrectAnswer(exercise: Exercise, answer: Int, elapsedMs: Int) {
        exerciseSource.recordAttempt(exercise, answer, elapsedMs.toLong())
        _answerResult.value = AnswerResult.Incorrect
    }

    private suspend fun handleSessionComplete() {
        // All exercises completed, calculate results and transition
        val results = resultsList()
        _uiState.value = UiState.SummaryScreen(results)
        _navigationEvents.emit(NavigationEvent.NavigateToSummary)
    }

    private fun updateWorkingSetForDebug() {
        if (!BuildConfig.DEBUG) {
            return
        }
        (exerciseSource as? AdaptiveExerciseSource)?.getWorkingSetForDebug()?.let {
            _workingSet.value = it
        }
    }

    fun onFeedbackAnimationFinished() {
        viewModelScope.launch {
            val isReview = currentLevelId?.contains("REVIEW", ignoreCase = true) == true

            when (_answerResult.value) {
                is AnswerResult.Incorrect -> {
                    if (!isReview) {
                        val equation = currentExercise?.equation?.getQuestionAsSolved(
                            currentExercise?.equation?.getExpectedResult()
                        ) ?: ""
                        _answerResult.value = AnswerResult.ShowCorrection(equation)
                    } else {
                        advanceToNextExercise()
                        _answerResult.value = AnswerResult.None
                    }
                }

                is AnswerResult.ShowCorrection, is AnswerResult.Correct -> {
                    advanceToNextExercise()
                    _answerResult.value = AnswerResult.None
                }

                else -> {
                    _answerResult.value = AnswerResult.None
                }
            }
            _recognizedText.value = null // Reset recognition state
        }
    }

    fun resultsList(): List<ResultDescription> {
        return exerciseHistory.map { exercise ->
            ResultDescription(
                exercise.equationString(), ResultStatus.fromBooleanPair(
                    exercise.solved, exercise.correct()
                ), exercise.timeTakenMillis ?: 0, exercise.speedBadge
            )
        }
    }

    fun onSummaryDone() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateBackToHome)
        }
    }

    fun restartExercises() {
        viewModelScope.launch {
            currentExerciseConfig?.let {
                startExercises(it)
                _navigationEvents.emit(NavigationEvent.NavigateToExercise(it.operation.name))
            }
        }
    }

    fun endSessionEarly() {
        viewModelScope.launch {
            exercisesRemaining = 0
            advanceToNextExercise()
        }
    }
}

object ExerciseViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val globals = App.app.globals
        return ExerciseViewModel(
            inkModelManager = globals.inkModelManager, exerciseSource = globals.exerciseSource
        ) as T
    }
}
