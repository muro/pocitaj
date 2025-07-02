package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule

enum class FeedbackType(val contentDescription: String) {
    CORRECT("Correct Answer Image"),
    INCORRECT("Incorrect Answer Image"),
    UNRECOGNIZED("Unrecognized Answer Image")
}

abstract class BaseExerciseUiTest {

    companion object {
        const val DEFAULT_UI_TIMEOUT = 1_000L
        // This is not a timeout, but a value to advance the test clock,
        // chosen to be slightly longer than the 1000ms recognition delay in the app.
        const val RECOGNITION_CLOCK_ADVANCE = 1100L
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ExerciseBookActivity>()

    @Before
    fun waitForAppToBeReady() {
        // Wait for the loading screen to disappear and the setup screen to be visible.
        // The "Start Addition" button uniquely identifies the ExerciseSetupScreen.
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithText("Start Addition")
                .fetchSemanticsNodes().size == 1
        }
    }

    fun navigateToExerciseType(exerciseTypeButtonText: String) {
        // Click on the button to start the specific exercise type
        composeTestRule.onNodeWithText(exerciseTypeButtonText).performClick()

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The InkCanvas is a good unique identifier for the ExerciseScreen.
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("InkCanvas")
                .fetchSemanticsNodes().size == 1
        }
    }

    /**
     * Simulates drawing an answer on the InkCanvas.
     *
     * @param answer The string that the fake recognizer should return.
     */
    fun drawAnswer(answer: String) {
        // Set the fake recognition result
        FakeInkModelManager.recognitionResult = answer

        // 1. Locate the drawing canvas
        val canvasNode = composeTestRule.onNodeWithTag("InkCanvas")
        canvasNode.assertExists("InkCanvas not found on screen.")

        // 2. Get canvas dimensions
        val canvasBounds = canvasNode.fetchSemanticsNode().boundsInRoot
        val canvasWidthPx = canvasBounds.width
        val canvasHeightPx = canvasBounds.height

        // Ensure dimensions are valid
        if (canvasWidthPx <= 0 || canvasHeightPx <= 0) {
            throw AssertionError("Canvas dimensions are invalid: Width=$canvasWidthPx, Height=$canvasHeightPx. Ensure the canvas is visible and has size.")
        }

        // 3. Get a generic drawing stroke (e.g., a simple line)
        val strokes = DrawingTestUtils.getPathForDigitOne(canvasWidthPx, canvasHeightPx)

        // 4. Perform drawing
        DrawingTestUtils.performStrokes(composeTestRule, canvasNode, strokes)

        // 5. Advance the clock to trigger recognition
        composeTestRule.mainClock.advanceTimeBy(RECOGNITION_CLOCK_ADVANCE)
    }

    /**
     * Verifies that the correct feedback image is displayed.
     *
     * @param type The type of feedback to expect.
     */
    fun verifyFeedback(type: FeedbackType) {
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodes(hasContentDescription(type.contentDescription))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}

