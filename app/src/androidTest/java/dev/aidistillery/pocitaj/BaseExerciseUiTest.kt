package dev.aidistillery.pocitaj

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.setup.StarRatingKey
import junit.framework.TestCase
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

    lateinit var globals: TestGlobals

    @Before
    open fun setup() {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        application.globals = TestGlobals(application)
        globals = application.globals as TestGlobals
        runBlocking {
            if (globals.userDao.getUser(1) == null) {
                globals.userDao.insert(User(id = 1, name = "Default User"))
            }
            globals.activeUserManager.init()
        }
        waitForAppToBeReady()
    }

    // @Before
    // Can't be @Before, as it could run before setup() above.
    fun waitForAppToBeReady() {
        // Wait for the loading screen to disappear and the setup screen to be visible.
        // The "Choose Your Challenge" title uniquely identifies the ExerciseSetupScreen.
        verifyOnExerciseSetupScreen()
    }

    @After
    fun tearDown() {
        globals.reset()
    }

    fun openOperationCard(operationSymbol: String) {
        composeTestRule.onNodeWithTag("operation_cards_container")
            .performScrollToNode(hasTestTag("operation_card_${operationSymbol}"))

        // Click on the card to start the specific exercise type
        composeTestRule.onNodeWithText(operationSymbol)
            .performVerifiedClick("Operation card '$operationSymbol'")

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onNodeWithText("Smart Practice")
                .isDisplayed()
        }
    }

    fun navigateToSmartPractice(operationSymbol: String) {
        openOperationCard(operationSymbol)

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
        openOperationCard(operationSymbol)

        // Explicitly scroll the parent list to bring the button into view
        val levelId = "SUB_REVIEW_1"
        composeTestRule.onNodeWithTag("operation_card_-")
            .performScrollToNode(matcher = hasTestTag("level_tile_${levelId}")) // -100_progress"))
        TestCase.assertEquals(getProgress(levelId), 100)

        // Click on the first "Review" button found
        composeTestRule.onAllNodesWithText("🧶")[0].performVerifiedClick("Review button")

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The InkCanvas is a good unique identifier for the ExerciseScreen.
        composeTestRule.waitUntil(timeoutMillis = 3 * DEFAULT_UI_TIMEOUT) {
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
        (globals.exerciseSource as ExerciseBook).loadSession(exercises)
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

    fun verifyOnExerciseSetupScreen() {
        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Choose Your Challenge")
                .fetchSemanticsNodes().size == 1
        }
        print("verifyOnExerciseSetupScreen: Current active ID: ${globals.activeUser.id} and name ${globals.activeUser.name}")
        composeTestRule.onNodeWithText("Choose Your Challenge").assertIsDisplayed()
    }

    private fun getCorrectAnswer(question: String): String {
        val parts = question.replace("?", "").trim().split(" ")
        val a = parts[0].toInt()
        val op = parts[1]
        val b = parts[2].toInt()

        return when (op) {
            "+" -> (a + b).toString()
            "-" -> (a - b).toString()
            "×" -> (a * b).toString()
            "÷" -> (a / b).toString()
            else -> throw IllegalArgumentException("Unknown operator: $op")
        }
    }

    fun getProgress(levelId: String): Int {
        val nodeInteraction = composeTestRule.onNodeWithTag("level_tile_${levelId}")
        nodeInteraction.assertIsDisplayed()

        val semanticsNode = nodeInteraction.fetchSemanticsNode()
        return semanticsNode.config[StarRatingKey]
    }

    fun answerAllQuestionsCorrectly(operationSymbol: String, levelId: String) {
        //openOperationCard(operationSymbol)
        composeTestRule.onNodeWithTag("level_tile_${levelId}").performClick()

        composeTestRule.onRoot().printToLog("whenLevelCompletedCorrectly_thenStarRatingIncreases")

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule
                .onAllNodesWithTag("InkCanvas")
                .fetchSemanticsNodes().size == 1
        }

        while (composeTestRule.onAllNodesWithTag("InkCanvas").fetchSemanticsNodes().isNotEmpty()) {
            val questionText = composeTestRule.onNodeWithTag("exercise_question")
                .fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first()
                .toString()
            val correctAnswer = getCorrectAnswer(questionText)
            drawAnswer(correctAnswer)
            verifyFeedback(FeedbackType.CORRECT)
            composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)
            composeTestRule.waitForIdle()
        }

        composeTestRule.waitUntil(timeoutMillis = DEFAULT_UI_TIMEOUT) {
            composeTestRule.onAllNodesWithText("Results").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("Back").performClick()
        verifyOnExerciseSetupScreen()
    }
}

