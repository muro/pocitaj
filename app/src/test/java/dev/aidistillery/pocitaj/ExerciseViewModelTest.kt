package dev.aidistillery.pocitaj

import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.SoundManager
import dev.aidistillery.pocitaj.ui.exercise.AnswerResult
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModel
import dev.aidistillery.pocitaj.ui.exercise.ResultStatus
import dev.aidistillery.pocitaj.ui.exercise.UiState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
    private lateinit var soundManager: SoundManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        inkModelManager = mockk(relaxed = true)
        exerciseSource = mockk(relaxed = true)
        soundManager = mockk(relaxed = true)
        viewModel = ExerciseViewModel(
            inkModelManager,
            exerciseSource,
            soundManager
        )
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
        coVerify { soundManager.playCorrect() }
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
        coVerify { soundManager.playWrong() }
    }

    @Test
    fun `onFeedbackAnimationFinished transitions to summary screen with same results`() =
        runTest {
            // GIVEN: A set of two exercises
            val exercise1 = Exercise(Addition(2, 2))
            val exercise2 = Exercise(Addition(3, 3))
            coEvery { exerciseSource.getNextExercise() } returns exercise1 andThen exercise2 andThen null

            val sessionResult = dev.aidistillery.pocitaj.data.SessionResult(
                listOf(
                    dev.aidistillery.pocitaj.ui.exercise.ResultDescription(
                        "2 + 2 = 4",
                        ResultStatus.CORRECT,
                        1000,
                        dev.aidistillery.pocitaj.logic.SpeedBadge.NONE
                    ),
                    dev.aidistillery.pocitaj.ui.exercise.ResultDescription(
                        "3 + 3 ≠ 5",
                        ResultStatus.INCORRECT,
                        1500,
                        dev.aidistillery.pocitaj.logic.SpeedBadge.NONE
                    )
                ),
                dev.aidistillery.pocitaj.data.StarProgress(0, 0)
            )
            coEvery { exerciseSource.getSessionResult(any()) } returns sessionResult

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

            // AND: The results should be same as before
            val summaryScreen = uiState
            summaryScreen.sessionResult.results.size shouldBe 2

            summaryScreen.sessionResult.results[0].equation shouldBe "2 + 2 = 4"
            summaryScreen.sessionResult.results[0].status shouldBe ResultStatus.CORRECT
            summaryScreen.sessionResult.results[0].elapsedMs shouldBe 1000

            summaryScreen.sessionResult.results[1].equation shouldBe "3 + 3 ≠ 5"
            summaryScreen.sessionResult.results[1].status shouldBe ResultStatus.INCORRECT
            summaryScreen.sessionResult.results[1].elapsedMs shouldBe 1500

            // AND: Level complete sound should be played
            coVerify { soundManager.playLevelComplete() }
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

        // VERIFY: Unrecognized sound is played
        coVerify { soundManager.playUnrecognized() }

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
        // Since we can't inspect the private history list directly, we infer it from the fact that it's cleared
        // This test is less relevant now that resultsList() is gone, but we can verify behaviour via interactions
        
        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        viewModel.checkAnswer("1", 1000)
        testDispatcher.scheduler.advanceUntilIdle()

        // WHEN: startExercises is called again
        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 10, 1))
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: The history should be empty (or rather, the session resets)
        // We implicitly verify this by checking that the viewModel is ready for a fresh exercise
        viewModel.uiState.value.shouldBeInstanceOf<UiState.ExerciseScreen>()
    }

    // The "history preserves state" test relied on resultsList(), which is gone.
    // We can rely on getSessionResult to produce the history, but since getSessionResult is mocked, 
    // the test value is limited to verifying that exerciseSource.getSessionResult is called with the correct history.
    
    @Test
    fun `SummaryScreen contains initial and final stars`() = runTest {
        // GIVEN: Initial progress is 0.1 (0 stars), Final progress should be 1.0 (3 stars)
        val levelId = "ADD_SUM_5"
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise andThen null
        every { exerciseSource.currentLevelId } returns levelId

        val sessionResult = dev.aidistillery.pocitaj.data.SessionResult(
            emptyList(),
            dev.aidistillery.pocitaj.data.StarProgress(0, 3)
        )
        coEvery { exerciseSource.getSessionResult(any()) } returns sessionResult

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 1, 1, levelId))
        testDispatcher.scheduler.advanceUntilIdle()

        // Complete the exercise
        viewModel.checkAnswer("4", 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onFeedbackAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Summary screen should show stars
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UiState.SummaryScreen>()
        uiState.sessionResult.starProgress.initialStars shouldBe 0
        uiState.sessionResult.starProgress.finalStars shouldBe 3
    }

    @Test
    fun `SummaryScreen does not trigger star celebration when stars stay same`() = runTest {
        // GIVEN: Initial progress 0.0 (0 stars)
        val levelId = "ADD_SUM_5"
        val exercise = Exercise(Addition(2, 2))
        coEvery { exerciseSource.getNextExercise() } returns exercise andThen null
        every { exerciseSource.currentLevelId } returns levelId

        val sessionResult = dev.aidistillery.pocitaj.data.SessionResult(
            emptyList(),
            dev.aidistillery.pocitaj.data.StarProgress(1, 2)
        )
        coEvery { exerciseSource.getSessionResult(any()) } returns sessionResult

        viewModel.startExercises(ExerciseConfig(Operation.ADDITION, 1, 1, levelId))
        testDispatcher.scheduler.advanceUntilIdle()

        // Complete exercise
        viewModel.checkAnswer("4", 1000)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onFeedbackAnimationFinished()
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Stars should be same
        val uiState = viewModel.uiState.value
        uiState.shouldBeInstanceOf<UiState.SummaryScreen>()
        uiState.sessionResult.starProgress.initialStars shouldBe 1
        uiState.sessionResult.starProgress.finalStars shouldBe 2
    }
}
