package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso // Added for pressBack
import org.junit.Test

class ExerciseFlowTest : BaseExerciseUiTest() {

    @Test
    fun testFullAppFlow_DrawingRecognized() {
        // 1. Navigate to "Start Addition"
        navigateToExerciseType("Start Addition")

        // 2. Draw the answer "1"
        drawAnswer("1")

        // 3. Verify that one of the feedback images appears
        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(
                hasContentDescription(feedbackImageContentDescriptions[0])
                    .or(hasContentDescription(feedbackImageContentDescriptions[1]))
                    .or(hasContentDescription(feedbackImageContentDescriptions[2]))
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testAnswerTwoQuestionsSequentially() {
        navigateToExerciseType("Start Addition")

        // First Question
        drawAnswer("1")

        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription(feedbackImageContentDescriptions[1]).fetchSemanticsNodes().isNotEmpty()
        }

        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)
        composeTestRule.mainClock.advanceTimeBy(2000)

        drawAnswer("1") // Assuming "1" is also the answer for the second question

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription(feedbackImageContentDescriptions[1]).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testFullNavigationFlow_SetupToSummaryToSetup() {
        navigateToExerciseType("Start Addition")

        // First Question
        drawAnswer("1")

        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription(feedbackImageContentDescriptions[1]).fetchSemanticsNodes().isNotEmpty()
        }

        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)
        composeTestRule.mainClock.advanceTimeBy(2000)

        drawAnswer("1")

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription(feedbackImageContentDescriptions[1]).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2000)

        // Verify Navigation to Summary Screen (ResultsScreen)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Done").fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate Back to Setup Screen
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        // Verify Navigation to Exercise Setup Screen
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Start Addition").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testNavigation_BackFromExerciseToSetup() {
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
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Start Addition").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testRecognition_UnrecognizedInput() {
        navigateToExerciseType("Start Addition")

        composeTestRule.waitForIdle()

        drawAnswer("") // Empty string for unrecognized input

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("Unrecognized Answer Image").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitForIdle() // Ensure UI is stable before test ends
    }
}
