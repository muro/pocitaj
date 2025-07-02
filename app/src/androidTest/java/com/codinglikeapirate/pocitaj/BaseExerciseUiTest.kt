package com.codinglikeapirate.pocitaj

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ExerciseBookActivity>()

    // Model Handling Note:
    // Currently, we are letting the app attempt to download the Digital Ink Recognition model
    // if it's not already present. If tests become flaky due to network issues or download times,
    // we will need to introduce a mechanism to mock or pre-prime the InkModelManager
    // (e.g., by using a fake/test version or ensuring the model is downloaded before tests run).

    @Before
    fun waitForAppToBeReady() {
        // Wait for the loading screen to disappear and setup screen to be visible.
        // Look for a button that uniquely identifies the ExerciseSetupScreen.
        // The text "Start Addition" is on one of the buttons.
        // Allow up to 30 seconds for the model to download, though it should be faster.
        var attempts = 0
        val maxAttempts = 30 // 30 attempts * 1000ms = 30 seconds
        val delayMillis = 1000L
        var setupScreenVisible = false

        while (attempts < maxAttempts && !setupScreenVisible) {
            try {
                composeTestRule.onNodeWithText("Start Addition").assertIsDisplayed()
                setupScreenVisible = true
            } catch (_: Exception) { // More specific: NoMatchingNodeException or AssertionError
                attempts++
                Thread.sleep(delayMillis)
            }
        }

        if (!setupScreenVisible) {
            throw AssertionError("ExerciseSetupScreen (with 'Start Addition' button) did not become visible after ${maxAttempts * delayMillis / 1000} seconds.")
        }
    }

    fun navigateToExerciseType(exerciseTypeButtonText: String) {
        // Click on the button to start the specific exercise type
        composeTestRule.onNodeWithText(exerciseTypeButtonText)
            //.assertIsDisplayed() // Optional: performClick will fail if not displayed
            .performClick()

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The InkCanvas is a good unique identifier for the ExerciseScreen.
        // Allow up to 5 seconds for the screen transition.
        var attempts = 0
        val maxAttempts = 10 // 10 attempts * 500ms = 5 seconds
        val delayMillis = 500L
        var exerciseScreenVisible = false

        while (attempts < maxAttempts && !exerciseScreenVisible) {
            try {
                composeTestRule.onNodeWithTag("InkCanvas").assertIsDisplayed()
                exerciseScreenVisible = true
            } catch (_: Exception) { // More specific: NoMatchingNodeException or AssertionError
                attempts++
                Thread.sleep(delayMillis)
            }
        }

        if (!exerciseScreenVisible) {
            throw AssertionError("ExerciseScreen (with 'InkCanvas') did not become visible after ${maxAttempts * delayMillis / 1000} seconds.")
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
        composeTestRule.mainClock.advanceTimeBy(1100) // Delay is 1000ms, advance slightly more
    }

    /**
     * Verifies that the correct feedback image is displayed.
     *
     * @param type The type of feedback to expect.
     */
    fun verifyFeedback(type: FeedbackType) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasContentDescription(type.contentDescription))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}

