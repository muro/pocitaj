package dev.aidistillery.pocitaj

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.Level
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModel
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModelFactory
import dev.aidistillery.pocitaj.ui.exercise.UiState

class ExerciseTestHelper(viewModelStoreOwner: ViewModelStoreOwner) {

    private val application =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
    private val globals = application.globals as TestGlobals

    private val viewModel: ExerciseViewModel by lazy {
        ViewModelProvider(
            viewModelStoreOwner,
            ExerciseViewModelFactory
        )[ExerciseViewModel::class.java]
    }

    fun startExercise(level: Level, exercises: List<Exercise>? = null) {
        exercises?.let {
            (globals.exerciseSource as ExerciseBook).loadSession(it)
        }
        val config = ExerciseConfig(
            levelId = level.id,
            operation = level.operation,
            count = exercises?.size ?: 10, // Default count for tests
            strategy = level.strategy,
            difficulty = 1
        )
        viewModel.startExercises(config)
    }

    fun getExerciseProblem(): String? {
        return when (val uiState = viewModel.uiState.value) {
            is UiState.ExerciseScreen -> uiState.currentExercise.equation.question()
            else -> null
        }
    }

    fun submitAnswer(answer: String, elapsedMs: Int = 1000) {
        viewModel.checkAnswer(answer, elapsedMs)
    }

    fun onFeedbackAnimationFinished() {
        viewModel.onFeedbackAnimationFinished()
    }

    fun getResults(): List<dev.aidistillery.pocitaj.ui.exercise.ResultDescription> {
        return viewModel.resultsList()
    }
}
