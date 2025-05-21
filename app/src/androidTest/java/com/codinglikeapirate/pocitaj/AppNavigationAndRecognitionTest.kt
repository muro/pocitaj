package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.*
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.espresso.Espresso // Added for pressBack
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationAndRecognitionTest : BaseExerciseUiTest() {

    private fun pollForContentDescription(descriptions: List<String>, timeoutMillis: Long): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < startTime + timeoutMillis) {
            for (desc in descriptions) {
                try {
                    // useUnmergedTree = true can be helpful if nodes are unexpectedly merged
                    composeTestRule.onNodeWithContentDescription(desc, useUnmergedTree = true).assertIsDisplayed()
                    return true // Found one and it's displayed
                } catch (e: AssertionError) {
                    // Node not found or not displayed, try next description or next poll attempt
                }
            }
            Thread.sleep(500) // Poll interval
        }
        return false // Timeout
    }

    private fun pollForNodeWithText(text: String, timeoutMillis: Long): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < startTime + timeoutMillis) {
            try {
                composeTestRule.onNodeWithText(text).assertIsDisplayed()
                return true // Found and displayed
            } catch (e: AssertionError) {
                // Node not found or not displayed, try next poll attempt
            }
            Thread.sleep(500) // Poll interval
        }
        return false // Timeout
    }


    @Test
    fun testFullAppFlow_DrawingRecognized() {
        // 1. Navigate to "Start Addition"
        navigateToExerciseType("Start Addition")

        // 2. Locate the drawing canvas
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found on screen.") // Ensure canvas is there

        // 3. Get canvas dimensions
        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val canvasWidthPx = canvasBounds.width
        val canvasHeightPx = canvasBounds.height

        // Ensure dimensions are valid
        if (canvasWidthPx <= 0 || canvasHeightPx <= 0) {
            throw AssertionError("Canvas dimensions are invalid: Width=$canvasWidthPx, Height=$canvasHeightPx. Ensure the canvas is visible and has size.")
        }

        // 4. Determine the answer to draw (digit "1" for this test)
        // 5. Get the drawing strokes
        val strokes = DrawingTestUtils.getPathForDigitOne(canvasWidthPx, canvasHeightPx)

        // 6. Perform drawing
        DrawingTestUtils.performStrokes(canvasNode, strokes)

        // 7. Verify that one of the feedback images appears
        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )
        val feedbackReceived = pollForContentDescription(feedbackImageContentDescriptions, 10000) // 10 seconds timeout
        assertTrue("No feedback image (Correct, Incorrect, or Unrecognized) was displayed after drawing '1'.", feedbackReceived)
    }

    @Test
    fun testAnswerTwoQuestionsSequentially() {
        navigateToExerciseType("Start Addition")

        // First Question
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found for the first question.")

        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val canvasWidth = canvasBounds.width
        val canvasHeight = canvasBounds.height

        if (canvasWidth <= 0 || canvasHeight <= 0) {
            throw AssertionError("Canvas dimensions are invalid for the first question: Width=$canvasWidth, Height=$canvasHeight.")
        }

        val strokesForOne = DrawingTestUtils.getPathForDigitOne(canvasWidth, canvasHeight)
        DrawingTestUtils.performStrokes(canvasNode, strokesForOne)

        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )
        val feedbackReceivedFirst = pollForContentDescription(feedbackImageContentDescriptions, 10000)
        assertTrue("Feedback image did not appear for the first answer (digit '1').", feedbackReceivedFirst)

        // Second Question
        composeTestRule.waitForIdle() // Wait for UI to settle (e.g., next question loaded)

        val canvasNodeSecond = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNodeSecond.assertExists("InkCanvas not found for the second question.")
        
        val canvasBoundsSecond = canvasNodeSecond.fetchSemanticsNode().boundsInRoot
        val canvasWidthSecond = canvasBoundsSecond.width
        val canvasHeightSecond = canvasBoundsSecond.height

        if (canvasWidthSecond <= 0 || canvasHeightSecond <= 0) {
            throw AssertionError("Canvas dimensions are invalid for the second question: Width=$canvasWidthSecond, Height=$canvasHeightSecond.")
        }

        val strokesForZero = DrawingTestUtils.getPathForDigitZero(canvasWidthSecond, canvasHeightSecond)
        DrawingTestUtils.performStrokes(canvasNodeSecond, strokesForZero)

        val feedbackReceivedSecond = pollForContentDescription(feedbackImageContentDescriptions, 10000)
        assertTrue("Feedback image did not appear for the second answer (digit '0').", feedbackReceivedSecond)
    }

    @Test
    fun testFullNavigationFlow_SetupToSummaryToSetup() {
        navigateToExerciseType("Start Addition") // Navigates to the first question

        val feedbackImageContentDescriptions = listOf(
            "Correct Answer Image",
            "Incorrect Answer Image",
            "Unrecognized Answer Image"
        )

        // Answer First Question
        val canvasNode1 = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode1.assertExists("InkCanvas not found for the first question.")
        val canvasBounds1 = canvasNode1.fetchSemanticsNode().boundsInRoot
        val strokesForOne = DrawingTestUtils.getPathForDigitOne(canvasBounds1.width, canvasBounds1.height)
        DrawingTestUtils.performStrokes(canvasNode1, strokesForOne)
        val feedback1 = pollForContentDescription(feedbackImageContentDescriptions, 10000)
        assertTrue("Feedback image did not appear for the first answer (digit '1').", feedback1)
        composeTestRule.waitForIdle()

        // Answer Second Question
        val canvasNode2 = composeTestRule.onNodeWithTag("InkCanvas") // Re-acquire
        canvasNode2.assertExists("InkCanvas not found for the second question.")
        val canvasBounds2 = canvasNode2.fetchSemanticsNode().boundsInRoot
        val strokesForTwo = DrawingTestUtils.getPathForDigitTwo(canvasBounds2.width, canvasBounds2.height)
        DrawingTestUtils.performStrokes(canvasNode2, strokesForTwo)
        val feedback2 = pollForContentDescription(feedbackImageContentDescriptions, 10000)
        assertTrue("Feedback image did not appear for the second answer (digit '2').", feedback2)
        composeTestRule.waitForIdle()

        // Verify Navigation to Summary Screen (ResultsScreen)
        // The ResultsScreen shows Text("Results"). Poll for it.
        val resultsScreenVisible = pollForNodeWithText("Results", 5000) // Poll for 5 seconds
        assertTrue("Results screen with text 'Results' did not appear.", resultsScreenVisible)

        // Navigate Back to Setup Screen
        composeTestRule.onNodeWithText("Back to menu").performClick()
        composeTestRule.waitForIdle()

        // Verify Navigation to Exercise Setup Screen
        // Check for the "Start Addition" button. Base class waitForAppToBeReady already checks this,
        // but we can check it explicitly here too.
        val setupScreenVisible = pollForNodeWithText("Start Addition", 5000) // Poll for 5 seconds
        assertTrue("ExerciseSetupScreen with 'Start Addition' button did not appear after returning from Results.", setupScreenVisible)
    }

    @Test
    fun testNavigation_BackFromExerciseToSetup() {
        // Navigate to the Exercise Screen
        navigateToExerciseType("Start Addition")

        // Verify that an element unique to the Exercise Screen is present
        // Using onNodeWithTag for the canvas is a good unique identifier
        composeTestRule.onNodeWithTag("InkCanvas").assertIsDisplayed()
        // Alternatively, could use the placeholder text if more robust:
        // val exerciseScreenVisible = pollForNodeWithText("Recognized Text: ???", 1000) // Short timeout, should be quick
        // assertTrue("Exercise screen (with 'Recognized Text: ???') did not appear.", exerciseScreenVisible)


        // Perform a back press
        Espresso.pressBack()

        // Wait for UI to settle
        composeTestRule.waitForIdle()

        // Verify that the ExerciseSetupScreen is displayed
        val setupScreenVisible = pollForNodeWithText("Start Addition", 5000) // Poll for 5 seconds
        assertTrue("After pressing back, ExerciseSetupScreen (with 'Start Addition' button) was not visible.", setupScreenVisible)
    }

    @Test
    fun testRecognition_UnrecognizedInput() {
        navigateToExerciseType("Start Addition")

        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found.") // Ensure canvas is there
        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val canvasWidth = canvasBounds.width
        val canvasHeight = canvasBounds.height

        // Ensure dimensions are valid before drawing
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            throw AssertionError("Canvas dimensions are invalid: Width=$canvasWidth, Height=$canvasHeight. Ensure the canvas is visible and has size.")
        }

        val scribbleStrokes = DrawingTestUtils.getPathForScribble(canvasWidth, canvasHeight)
        DrawingTestUtils.performStrokes(canvasNode, scribbleStrokes)

        val unrecognizedFeedbackDisplayed = pollForContentDescription(
            listOf("Unrecognized Answer Image"),
            10000 // 10-second timeout
        )
        assertTrue("Unrecognized feedback image did not appear after drawing a scribble.", unrecognizedFeedbackDisplayed)

        composeTestRule.waitForIdle() // Ensure UI is stable before test ends
    }
}
