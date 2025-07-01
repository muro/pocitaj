package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso // Added for pressBack
import org.junit.Before
import org.junit.Test

class ExerciseFlowTest : BaseExerciseUiTest() {

    @Before
    fun setup() {
        FakeInkModelManager.recognitionResult = "1"
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

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithTag("InkCanvas").isDisplayed()
        }

        // 4. Determine the answer to draw (digit "1" for this test)
        // 5. Get the drawing strokes
        val strokes = DrawingTestUtils.getPathForDigitOne(canvasWidthPx, canvasHeightPx)

        // 6. Perform drawing
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokes)

        // Advance the clock to bypass the recognition delay in LaunchedEffect
        composeTestRule.mainClock.advanceTimeBy(1100) // Delay is 1000ms, advance slightly more

        // 7. Verify that one of the feedback images appears
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
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found for the first question.")

        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val canvasWidth = canvasBounds.width
        val canvasHeight = canvasBounds.height

        if (canvasWidth <= 0 || canvasHeight <= 0) {
            throw AssertionError("Canvas dimensions are invalid for the first question: Width=$canvasWidth, Height=$canvasHeight.")
        }

        val strokesForOne = DrawingTestUtils.getPathForDigitOne(canvasWidth, canvasHeight)
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokesForOne)
        composeTestRule.mainClock.advanceTimeBy(1100)

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

        val canvasNodeSecond = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNodeSecond.assertExists("InkCanvas not found for the second question.")
        
        val canvasBoundsSecond = canvasNodeSecond.fetchSemanticsNode().boundsInRoot
        val canvasWidthSecond = canvasBoundsSecond.width
        val canvasHeightSecond = canvasBoundsSecond.height

        if (canvasWidthSecond <= 0 || canvasHeightSecond <= 0) {
            throw AssertionError("Canvas dimensions are invalid for the second question: Width=$canvasWidthSecond, Height=$canvasHeightSecond.")
        }

        val strokesForZero = DrawingTestUtils.getPathForDigitOne(canvasWidthSecond, canvasHeightSecond) // getPathForDigitZero
        DrawingTestUtils.performStrokes(composeTestRule, canvasNodeSecond, strokesForOne)
        composeTestRule.mainClock.advanceTimeBy(1100)

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription(feedbackImageContentDescriptions[1]).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testFullNavigationFlow_SetupToSummaryToSetup() {
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
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokesForOne)
        composeTestRule.mainClock.advanceTimeBy(1100)

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

        val canvasNodeSecond = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNodeSecond.assertExists("InkCanvas not found for the second question.")

        val canvasBoundsSecond = canvasNodeSecond.fetchSemanticsNode().boundsInRoot
        val canvasWidthSecond = canvasBoundsSecond.width
        val canvasHeightSecond = canvasBoundsSecond.height

        if (canvasWidthSecond <= 0 || canvasHeightSecond <= 0) {
            throw AssertionError("Canvas dimensions are invalid for the second question: Width=$canvasWidthSecond, Height=$canvasHeightSecond.")
        }

        val strokesForZero = DrawingTestUtils.getPathForDigitOne(canvasWidthSecond, canvasHeightSecond) // getPathForDigitZero
        DrawingTestUtils.performStrokes(composeTestRule, canvasNodeSecond, strokesForOne)
        composeTestRule.mainClock.advanceTimeBy(1100)

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
        FakeInkModelManager.recognitionResult = ""
        navigateToExerciseType("Start Addition")

        composeTestRule.waitForIdle()

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
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, scribbleStrokes)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("Unrecognized Answer Image").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitForIdle() // Ensure UI is stable before test ends
    }
}
