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
}

