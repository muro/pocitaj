package dev.aidistillery.pocitaj

import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.exercise.AnswerResult
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModel
import dev.aidistillery.pocitaj.ui.exercise.ResultStatus
import dev.aidistillery.pocitaj.ui.exercise.UiState
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ExerciseViewModelTest {

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var inkModelManager: InkModelManager
    private lateinit var exerciseSource: ExerciseSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        inkModelManager = mockk(relaxed = true)
        exerciseSource = mockk(relaxed = true)
        viewModel = ExerciseViewModel(inkModelManager, exerciseSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkAnswer with correct answer sets Correct state`() = runTest {
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("4", 1000)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.answerResult.value shouldBe AnswerResult.Correct
    }

    @Test
    fun `checkAnswer with incorrect answer sets Incorrect state`() = runTest {
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("5", 1000)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.answerResult.value shouldBe AnswerResult.Incorrect
    }

    @Test
    fun `onFeedbackAnimationFinished transitions to summary screen with correct results`() =
        runTest {
            // GIVEN: A set of two exercises
            val exercise1 = Exercise(Addition(2, 2))
            val exercise2 = Exercise(Addition(3, 3))
            coEvery { exerciseSource.getNextExercise() } returns exercise1 andThen exercise2 andThen null

            viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 2))
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN: The first answer is correct
            viewModel.checkAnswer("4", 1000)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onFeedbackAnimationFinished()
            testDispatcher.scheduler.advanceUntilIdle()

            // WHEN: The second answer is incorrect
            viewModel.checkAnswer("5", 1500)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onFeedbackAnimationFinished() // This will now go to ShowCorrection
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onFeedbackAnimationFinished() // This will advance to the next exercise (which is null)
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN: The UI state should be SummaryScreen
            val uiState = viewModel.uiState.value
            uiState.shouldBeInstanceOf<UiState.SummaryScreen>()

            // AND: The results should be correct
            val summaryScreen = uiState
            summaryScreen.results.size shouldBe 2

            summaryScreen.results[0].equation shouldBe "2 + 2 = 4"
            summaryScreen.results[0].status shouldBe ResultStatus.CORRECT
            summaryScreen.results[0].elapsedMs shouldBe 1000

            summaryScreen.results[1].equation shouldBe "3 + 3 ≠ 5"
            summaryScreen.results[1].status shouldBe ResultStatus.INCORRECT
            summaryScreen.results[1].elapsedMs shouldBe 1500
        }

    @Test
    fun `checkAnswer calls repository to record attempt`() = runTest {
        val exercise = Exercise(Addition(3, 4))
        coEvery { exerciseSource.getNextExercise() } returns exercise

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
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

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 2))
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        initialState.shouldBeInstanceOf<UiState.ExerciseScreen>()
        initialState.currentExercise shouldBe exercise1

        // WHEN: An unrecognized answer is submitted and the animation finishes
        viewModel.checkAnswer("?", 1000) // Invalid answer
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.answerResult.value shouldBe AnswerResult.Unrecognized

        viewModel.onFeedbackAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The view model should NOT have advanced to the next exercise
        val finalState = viewModel.uiState.value
        finalState.shouldBeInstanceOf<UiState.ExerciseScreen>()
        finalState.currentExercise shouldBe exercise1
    }

    @Test
    fun `startExercises clears history`() = runTest {
        // GIVEN: The history is not empty
        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        viewModel.checkAnswer("1", 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resultsList().isNotEmpty().shouldBeTrue()

        // WHEN: startExercises is called again
        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The history should be empty
        viewModel.resultsList().isEmpty().shouldBeTrue()
    }

    @Test
    fun `history preserves state of multiple attempts on same question`() = runTest {
        // GIVEN: An exercise is presented
        val exercise = Exercise(Addition(7, 7))
        coEvery { exerciseSource.getNextExercise() } returns exercise andThen null

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: The user makes an unrecognized attempt, then a correct attempt
        viewModel.checkAnswer("xyz", 1000) // Unrecognized
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkAnswer("14", 1500) // Correct
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onFeedbackAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The results list should contain two distinct entries
        val results = viewModel.resultsList()
        results.size shouldBe 2

        // Verify the first attempt (unrecognized)
        results[0].equation shouldBe "7 + 7 = ?"
        results[0].status shouldBe ResultStatus.NOT_RECOGNIZED

        // Verify the second attempt (correct)
        results[1].equation shouldBe "7 + 7 = 14"
        results[1].status shouldBe ResultStatus.CORRECT
    }
}
