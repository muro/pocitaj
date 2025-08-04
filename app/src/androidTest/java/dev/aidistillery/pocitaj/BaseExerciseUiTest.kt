package dev.aidistillery.pocitaj

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.logic.Exercise
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule

enum class FeedbackType(val contentDescription: String) {
    CORRECT("Correct Answer Image"),
    INCORRECT("Incorrect Answer Image"),
    UNRECOGNIZED("Unrecognized Answer Image")
}

fun SemanticsNodeInteraction.performVerifiedClick(nodeDescription: String): SemanticsNodeInteraction {
    assertExists("Could not find the '$nodeDescription'. Check the finder logic.")
    assertIsEnabled()
    return performClick()
}

abstract class BaseExerciseUiTest {

    companion object {
        const val DEFAULT_UI_TIMEOUT = 1_000L

        // This is not a timeout, but a value to advance the test clock,
        // chosen to be slightly longer than the 1000ms recognition delay in the app.
        const val RECOGNITION_CLOCK_ADVANCE = 1100L
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        runBlocking {
            if (application.database.userDao().getUser(1) == null) {
                application.database.userDao().insert(User(id = 1, name = "Default User"))
            }
        }
    }

    @Before
    fun waitForAppToBeReady() {
        // Wait for the loading screen to disappear and the setup screen to be visible.
        // The "Choose Your Challenge" title uniquely identifies the ExerciseSetupScreen.
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithText("Choose Your Challenge")
                .fetchSemanticsNodes().size == 1
        }
    }

    @After
    fun tearDown() {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        runBlocking {
            application.database.clearAllTables()
        }
    }

    fun navigateToOperation(operationSymbol: String) {
        // Click on the card to start the specific exercise type
        composeTestRule.onNodeWithText(operationSymbol)
            .performVerifiedClick("Operation card '$operationSymbol'")

        // Click on the "Practice (Smart)" button
        composeTestRule.onNodeWithText("Smart Practice")
            .performVerifiedClick("'Smart Practice' button")

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The InkCanvas is a good unique identifier for the ExerciseScreen.
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("InkCanvas")
                .fetchSemanticsNodes().size == 1
        }
    }

    fun navigateToReviewOperation(operationSymbol: String) {
        // Click on the card to start the specific exercise type
        composeTestRule.onNodeWithText(operationSymbol)
            .performVerifiedClick("Operation card '$operationSymbol'")

        // Explicitly scroll the parent list to bring the button into view
        composeTestRule.onNodeWithTag("setup_lazy_column")
            .performScrollToNode(hasText("🧶"))

        // Click on the first "Review" button found
        composeTestRule.onAllNodesWithText("🧶")[0].performVerifiedClick("Review button")

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The InkCanvas is a good unique identifier for the ExerciseScreen.
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("InkCanvas")
                .fetchSemanticsNodes().size == 1
        }
    }

    /**
     * A helper function to set a specific list of exercises for a test run.
     * This is the primary way to create a predictable state for UI tests.
     */
    fun setExercises(exercises: List<Exercise>) {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        (application.exerciseSource as ExerciseBook).loadSession(exercises)
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
        val strokes = DrawingTestUtils.getDefaultDrawingPath(canvasWidthPx, canvasHeightPx)

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

    fun assertNoFeedbackIsShown() {
        composeTestRule.onNode(hasContentDescription(FeedbackType.CORRECT.contentDescription))
            .assertDoesNotExist()
        composeTestRule.onNode(hasContentDescription(FeedbackType.INCORRECT.contentDescription))
            .assertDoesNotExist()
        composeTestRule.onNode(hasContentDescription(FeedbackType.UNRECOGNIZED.contentDescription))
            .assertDoesNotExist()
    }
}

