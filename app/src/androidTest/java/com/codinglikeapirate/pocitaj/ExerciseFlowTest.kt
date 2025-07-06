package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso
import org.junit.Test

class ExerciseFlowTest : BaseExerciseUiTest() {

    @Test
    fun whenCorrectAnswerDrawn_thenCorrectFeedbackIsShown() {
        // 1. Navigate to "Start Addition"
        navigateToExerciseType("Addition")

        // 2. Create a custom exercise book and load a test session
        val exerciseBook = ExerciseBook()
        exerciseBook.loadSession(listOf(Exercise(Addition(5, 3)))) // 5 + 3 = 8

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
        navigateToExerciseType("Addition")

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
        // 1. Navigate to "Addition"
        navigateToExerciseType("Addition")

        // 2. Create a custom exercise book and load a test session with two exercises
        val exerciseBook = ExerciseBook()
        exerciseBook.loadSession(listOf(Exercise(Addition(1, 1)), Exercise(Addition(2, 2))))

        // 3. Set the custom exercise book on the view model
        lateinit var viewModel: ExerciseBookViewModel
        composeTestRule.activityRule.scenario.onActivity { activity ->
            viewModel = ViewModelProvider(
                activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
            ).get(ExerciseBookViewModel::class.java)
            viewModel.setExerciseBookForTesting(exerciseBook)
        }

        // 4. Wait for the UI to settle
        composeTestRule.waitForIdle()

        // 5. Answer the two questions
        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT) // This is incorrect, but the test is about flow
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2000)

        drawAnswer("1")
        verifyFeedback(FeedbackType.INCORRECT)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2000)

        // 6. Verify Navigation to Summary Screen (ResultsScreen)
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Done").fetchSemanticsNodes().isNotEmpty()
        }

        // 7. Navigate Back to Setup Screen
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        // 8. Verify Navigation to Exercise Setup Screen
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Choose Your Challenge").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun whenSystemBackButtonPressedOnExerciseScreen_thenNavigatesToSetup() {
        // Navigate to the Exercise Screen
        navigateToExerciseType("Addition")

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
        navigateToExerciseType("Addition")

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
        // 1. Navigate and set up a custom exercise
        navigateToExerciseType("Addition")
        val exerciseBook = ExerciseBook()
        exerciseBook.loadSession(listOf(Exercise(Addition(100, 23)))) // Answer is 123
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

