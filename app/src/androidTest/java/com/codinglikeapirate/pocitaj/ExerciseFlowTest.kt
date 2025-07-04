package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.*
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso // Added for pressBack
import org.junit.Test

class ExerciseFlowTest : BaseExerciseUiTest() {

    @Test
    fun whenCorrectAnswerDrawn_thenCorrectFeedbackIsShown() {
        // 1. Navigate to "Start Addition"
        navigateToExerciseType("Start Addition")

        // 2. Create a custom exercise book
        val exerciseBook = ExerciseBook()
        exerciseBook.addExercise(Exercise(Addition(5, 3))) // 5 + 3 = 8

        // 3. Set the custom exercise book on the view model
        lateinit var viewModel: ExerciseBookViewModel
        composeTestRule.activityRule.scenario.onActivity { activity ->
            viewModel = ViewModelProvider(activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application))
                .get(ExerciseBookViewModel::class.java)
            viewModel.setExerciseBookForTesting(exerciseBook)
        }

        // 4. Wait for the UI to settle
        composeTestRule.waitForIdle()

        // 5. Draw the correct answer "8"
        drawAnswer("8")

        // 6. Verify that the correct feedback image appears
        verifyFeedback(FeedbackType.CORRECT)
    }

    @Test
    fun whenTwoQuestionsAnswered_thenUIAdvancesAndShowsFeedback() {
        navigateToExerciseType("Start Addition")

        // First Question
        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)


        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)
        composeTestRule.mainClock.advanceTimeBy(2000)

        drawAnswer("1") // Assuming "1" is also the answer for the second question
        verifyFeedback(FeedbackType.INCORRECT)
    }

    @Test
    fun whenFullExerciseLoopCompleted_thenReturnsToSetupScreen() {
        navigateToExerciseType("Start Addition")

        // First Question
        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)

        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)
        composeTestRule.mainClock.advanceTimeBy(2000)

        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)

        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2000)

        // Verify Navigation to Summary Screen (ResultsScreen)
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Done").fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate Back to Setup Screen
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        // Verify Navigation to Exercise Setup Screen
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Start Addition").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenSystemBackButtonPressedOnExerciseScreen_thenNavigatesToSetup() {
        // Navigate to the Exercise Screen
        navigateToExerciseType("Start Addition")

        // Verify that an element unique to the Exercise Screen is present
        // Using onNodeWithTag for the canvas is a good unique identifier
        composeTestRule.onNodeWithTag("InkCanvas").assertIsDisplayed()

        // Perform a back press
        Espresso.pressBack()

        // Wait for UI to settle
        composeTestRule.waitForIdle()

        // Verify that the ExerciseSetupScreen is displayed
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Start Addition").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenUnrecognizedAnswerDrawn_thenUnrecognizedFeedbackIsShown() {
        navigateToExerciseType("Start Addition")

        composeTestRule.waitForIdle()

        drawAnswer("") // Empty string for unrecognized input
        verifyFeedback(FeedbackType.UNRECOGNIZED)


        composeTestRule.waitForIdle() // Ensure UI is stable before test ends
    }

    private fun drawDigitWithDelay(delay: Long) {
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found on screen.")
        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val strokes = DrawingTestUtils.getPathForDigitOne(canvasBounds.width, canvasBounds.height)
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokes)
        if (delay > 0) {
            composeTestRule.mainClock.advanceTimeBy(delay)
        }
    }

    @Test
    fun threeDigitNumber_withDelays_isRecognizedCorrectly() {
        // 1. Navigate and set up a custom exercise
        navigateToExerciseType("Start Addition")
        val exerciseBook = ExerciseBook()
        exerciseBook.addExercise(Exercise(Addition(100, 23))) // Answer is 123
        lateinit var viewModel: ExerciseBookViewModel
        composeTestRule.activityRule.scenario.onActivity { activity ->
            viewModel = ViewModelProvider(activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application))
                .get(ExerciseBookViewModel::class.java)
            viewModel.setExerciseBookForTesting(exerciseBook)
        }
        composeTestRule.waitForIdle()

        // 2. Draw the digits with delays, updating the fake result each time
        FakeInkModelManager.recognitionResult = "1"
        drawDigitWithDelay(800) // Draw "1", wait 0.8s
        assertNoFeedbackIsShown()

        FakeInkModelManager.recognitionResult = "12"
        drawDigitWithDelay(800) // Draw "2", wait 0.8s
        assertNoFeedbackIsShown()

        FakeInkModelManager.recognitionResult = "123"
        drawDigitWithDelay(0)   // Draw "3", no extra wait

        // 3. Wait for the final recognition timer to fire
        composeTestRule.mainClock.advanceTimeBy(RECOGNITION_CLOCK_ADVANCE)

        // 4. Verify that the correct feedback is shown
        verifyFeedback(FeedbackType.CORRECT)
    }
}

