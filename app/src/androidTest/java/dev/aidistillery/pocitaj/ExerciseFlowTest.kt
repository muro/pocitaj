package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Exercise
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

private const val RESULT_ANIMATION_PROGRESS_TIME : Long = 600

class ExerciseFlowTest : BaseExerciseUiTest() {

    @Suppress("EmptyMethod")
    @Before
    fun setupUser() {
        // TODO: Rely on the default user created by the application's onCreate callback.
        // val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        runBlocking {
            // application.database.userDao().insert(User(id = 1, name = "test_user"))
        }
    }

    @Test
    fun whenCorrectAnswerDrawn_thenCorrectFeedbackIsShown() {
        // 1. Set the exercises for the test
        setExercises(listOf(Exercise(Addition(5, 3)))) // 5 + 3 = 8

        // 2. Navigate to "Start Addition"
        navigateToOperation("+")

        // 3. Wait for the UI to settle
        composeTestRule.waitForIdle()

        // 4. Draw the correct answer "8"
        drawAnswer("8")

        // 5. Verify that the correct feedback image appears
        verifyFeedback(FeedbackType.CORRECT)
    }

    @Test
    fun whenTwoQuestionsAnswered_thenUIAdvancesAndShowsFeedback() {
        setExercises(listOf(Exercise(Addition(1, 1)), Exercise(Addition(2, 2))))
        navigateToOperation("+")

        // First Question
        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)

        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)

        drawAnswer("1") // Assuming "1" is also the answer for the second question
        verifyFeedback(FeedbackType.INCORRECT)
    }

    @Test
    fun whenFullExerciseLoopCompleted_thenReturnsToSetupScreen() {
        // 1. Set the exercises for the test
        setExercises(listOf(Exercise(Addition(1, 1)), Exercise(Addition(2, 2))))

        // 2. Navigate to "Addition"
        navigateToOperation("+")

        // 3. Wait for the UI to settle
        composeTestRule.waitForIdle()

        // 4. Answer the two questions
        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT) // This is incorrect, but the test is about flow
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)

        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)

        // 5. Verify Navigation to Summary Screen (ResultsScreen)
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Results").fetchSemanticsNodes().isNotEmpty()
        }

        // 6. Navigate Back to Setup Screen
        composeTestRule.onNodeWithTag("Back").performClick()
        composeTestRule.waitForIdle()

        // 7. Verify Navigation to Exercise Setup Screen
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Choose Your Challenge").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenSystemBackButtonPressedOnExerciseScreen_thenNavigatesToSetup() {
        // Set a dummy exercise to prevent crashes
        setExercises(listOf(Exercise(Addition(1, 1))))

        // Navigate to the Exercise Screen
        navigateToOperation("+")

        // Verify that an element unique to the Exercise Screen is present
        // Using onNodeWithTag for the canvas is a good unique identifier
        composeTestRule.onNodeWithTag("InkCanvas").assertIsDisplayed()

        // Perform a back press
        Espresso.pressBack()

        // Wait for UI to settle
        composeTestRule.waitForIdle()

        // Verify that the ExerciseSetupScreen is displayed
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Choose Your Challenge").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenUnrecognizedAnswerDrawn_thenUnrecognizedFeedbackIsShown() {
        setExercises(listOf(Exercise(Addition(1, 1))))
        navigateToOperation("+")

        composeTestRule.waitForIdle()

        drawAnswer("") // Empty string for unrecognized input
        verifyFeedback(FeedbackType.UNRECOGNIZED)


        composeTestRule.waitForIdle() // Ensure UI is stable before test ends
    }

    private fun drawDigitWithDelay(delay: Long) {
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found on screen.")
        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val strokes = DrawingTestUtils.getDefaultDrawingPath(canvasBounds.width, canvasBounds.height)
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokes)
        if (delay > 0) {
            composeTestRule.mainClock.advanceTimeBy(delay)
        }
    }

    @Test
    fun threeDigitNumber_withDelays_isRecognizedCorrectly() {
        // 1. Set up a custom exercise
        setExercises(listOf(Exercise(Addition(100, 23)))) // Answer is 123

        // 2. Navigate
        navigateToOperation("+")
        composeTestRule.waitForIdle()

        // 3. Draw the digits with delays, updating the fake result each time
        FakeInkModelManager.recognitionResult = "1"
        drawDigitWithDelay(800) // Draw "1", wait 0.8s
        assertNoFeedbackIsShown()

        FakeInkModelManager.recognitionResult = "12"
        drawDigitWithDelay(800) // Draw "2", wait 0.8s
        assertNoFeedbackIsShown()

        FakeInkModelManager.recognitionResult = "123"
        drawDigitWithDelay(0)   // Draw "3", no extra wait

        // 4. Wait for the final recognition timer to fire
        composeTestRule.mainClock.advanceTimeBy(RECOGNITION_CLOCK_ADVANCE)

        // 5. Verify that the correct feedback is shown
        verifyFeedback(FeedbackType.CORRECT)
    }

    @Test
    fun whenIncorrectAnswerInTraining_thenShowsCorrection() {
        // 1. Set up exercises
        setExercises(listOf(Exercise(Addition(2, 2)), Exercise(Addition(3, 3))))

        // 2. Navigate to a non-review level
        navigateToOperation("+") // "Smart Practice" is a training level

        // 3. Answer incorrectly
        drawAnswer("5")

        // 4. Verify sad icon is shown
        verifyFeedback(FeedbackType.INCORRECT)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME) // Wait for sad animation

        // 5. Verify teacher icon and correct answer are shown
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Teacher Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 + 2 = 4").assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(4000) // Wait for teacher animation

        // 6. Verify it moves to the next question
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("3 + 3 = ?").assertIsDisplayed()
    }

    @Test
    fun whenIncorrectAnswerInReview_thenSkipsCorrection() {
        // 1. Master prerequisite levels
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val factMasteryDao = application.database.factMasteryDao()
        runBlocking {
            Curriculum.SubtractionFrom5.getAllPossibleFactIds().forEach { factId ->
                factMasteryDao.upsert(FactMastery(factId, 1, 5, 0))
            }
            Curriculum.SubtractionFrom10.getAllPossibleFactIds().forEach { factId ->
                factMasteryDao.upsert(FactMastery(factId, 1, 5, 0))
            }
        }

        // 2. Set up exercises for a review level
        setExercises(listOf(Exercise(Addition(1, 1)), Exercise(Addition(4, 4))))

        // 3. Navigate to a review level
        navigateToReviewOperation("-")

        // 4. Answer incorrectly
        drawAnswer("3")

        // 5. Verify sad icon is shown
        verifyFeedback(FeedbackType.INCORRECT)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME) // Wait for sad animation

        // 6. Verify it moves directly to the next question without showing the teacher
        composeTestRule.onNodeWithContentDescription("Teacher Image").assertDoesNotExist()
        composeTestRule.onNodeWithText("4 + 4 = ?").assertIsDisplayed()
    }
    
    // TODO: Create a new test file (e.g., "LevelSelectionTest.kt") that does not rely on the
    //       ExerciseBook test double. This new test should verify that clicking a specific
    //       level button on the ExerciseSetupScreen correctly starts a session with exercises
    //       only from that level.
}

