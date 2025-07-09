package com.codinglikeapirate.pocitaj

import com.codinglikeapirate.pocitaj.data.ExerciseSource
import com.codinglikeapirate.pocitaj.logic.Addition
import com.codinglikeapirate.pocitaj.logic.Exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ExerciseBookViewModelTest {

    private lateinit var viewModel: ExerciseBookViewModel
    private lateinit var inkModelManager: InkModelManager
    private lateinit var exerciseSource: ExerciseSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        inkModelManager = mockk(relaxed = true)
        exerciseSource = mockk(relaxed = true)
        viewModel = ExerciseBookViewModel(inkModelManager, exerciseSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkAnswer with correct answer sets Correct state`() = runTest {
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig("addition", count = 1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("4", 1000)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AnswerResult.Correct, viewModel.answerResult.value)
    }

    @Test
    fun `checkAnswer with incorrect answer sets Incorrect state`() = runTest {
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig("addition", count = 1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("5", 1000)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AnswerResult.Incorrect, viewModel.answerResult.value)
    }

    @Test
    fun `onResultAnimationFinished transitions to summary screen with correct results`() = runTest {
        // GIVEN: A set of two exercises
        val exercise1 = Exercise(Addition(2, 2))
        val exercise2 = Exercise(Addition(3, 3))
        coEvery { exerciseSource.getNextExercise() } returns exercise1 andThen exercise2 andThen null

        viewModel.startExercises(ExerciseConfig("addition", count = 2))
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: The first answer is correct
        viewModel.checkAnswer("4", 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onResultAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: The second answer is incorrect
        viewModel.checkAnswer("5", 1500)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onResultAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The UI state should be SummaryScreen
        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.SummaryScreen)

        // AND: The results should be correct
        val summaryScreen = uiState as UiState.SummaryScreen
        assertEquals(2, summaryScreen.results.size)

        assertEquals("2 + 2 = 4", summaryScreen.results[0].equation)
        assertEquals(ResultStatus.CORRECT, summaryScreen.results[0].status)
        assertEquals(1000, summaryScreen.results[0].elapsedMs)

        assertEquals("3 + 3 ≠ 5", summaryScreen.results[1].equation)
        assertEquals(ResultStatus.INCORRECT, summaryScreen.results[1].status)
        assertEquals(1500, summaryScreen.results[1].elapsedMs)
    }

    @Test
    fun `checkAnswer calls repository to record attempt`() = runTest {
        val exercise = Exercise(Addition(3, 4))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig("addition", count = 1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("7", 1234)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { exerciseSource.recordAttempt(any(), 7, 1234) }
    }

    @Test
    fun `onResultAnimationFinished stays on same exercise on an unrecognized answer`() = runTest {
        // GIVEN: An exercise is underway
        val exercise1 = Exercise(Addition(2, 2))
        val exercise2 = Exercise(Addition(3, 3))
        coEvery { exerciseSource.getNextExercise() } returns exercise1 andThen exercise2

        viewModel.startExercises(ExerciseConfig("addition", count = 2))
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertTrue(initialState is UiState.ExerciseScreen && initialState.currentExercise == exercise1)

        // WHEN: An unrecognized answer is submitted and the animation finishes
        viewModel.checkAnswer("?", 1000) // Invalid answer
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AnswerResult.Unrecognized, viewModel.answerResult.value)

        viewModel.onResultAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The view model should NOT have advanced to the next exercise
        val finalState = viewModel.uiState.value
        assertTrue(finalState is UiState.ExerciseScreen && finalState.currentExercise == exercise1)
    }

    @Test
    fun `startExercises clears history`() = runTest {
        // GIVEN: The history is not empty
        viewModel.startExercises(ExerciseConfig("addition", count = 1))
        viewModel.checkAnswer("1", 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.resultsList().isNotEmpty())

        // WHEN: startExercises is called again
        viewModel.startExercises(ExerciseConfig("addition", count = 1))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The history should be empty
        assertTrue(viewModel.resultsList().isEmpty())
    }
}
