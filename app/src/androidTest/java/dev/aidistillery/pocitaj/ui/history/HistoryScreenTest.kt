package dev.aidistillery.pocitaj.ui.history

import android.util.Log
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.BaseExerciseUiTest
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.TestApp
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class HistoryScreenTest : BaseExerciseUiTest() {

    @Test
    fun historyScreen_displaysHistory() {
        // GIVEN: A set of exercise attempts for today in the database
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val exerciseAttemptDao = application.globals.exerciseAttemptDao

        runBlocking {
            if (application.globals.userDao.getUser(1) == null) {
                false.shouldBeTrue()
                Log.e("HistoryScreenTest", "User with ID 1 not found in the database.")
            }
        }
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        
        runBlocking {
            exerciseAttemptDao.insert(
                ExerciseAttempt(
                    problemText = "2 + 2 = ?",
                    submittedAnswer = 4,
                    wasCorrect = true,
                    correctAnswer = 4,
                    durationMs = 1000,
                    timestamp = now,
                    userId = 1,
                    logicalOperation = Operation.ADDITION
                )
            )
            exerciseAttemptDao.insert(
                ExerciseAttempt(
                    problemText = "3 + 3 = ?",
                    submittedAnswer = 5,
                    wasCorrect = false,
                    correctAnswer = 6,
                    durationMs = 2100,
                    timestamp = now - 1000,
                    userId = 1,
                    logicalOperation = Operation.ADDITION
                )
            )
        }

        // WHEN: The user navigates to the history screen
        composeTestRule.onNodeWithContentDescription("My Progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.waitForIdle()

        // THEN: The history should be displayed (today is selected by default)
        composeTestRule.onNodeWithTag("activity_heatmap").assertExists()
        composeTestRule.onNodeWithTag("heatmap_day_$today").assertExists()
        
        composeTestRule.onNodeWithText("2 + 2 = 4").assertExists()
        composeTestRule.onNodeWithContentDescription("Correct").assertExists()

        composeTestRule.onNodeWithText("3 + 3 = 5").assertExists()
        composeTestRule.onNodeWithContentDescription("Incorrect").assertExists()

        composeTestRule.onNodeWithText("1.0s").assertExists()
        composeTestRule.onNodeWithText("2.1s").assertExists()
    }

    @Test
    fun historyScreen_displaysEmptyState() {
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        runBlocking {
            if (application.globals.userDao.getUser(1) == null) {
                false.shouldBeTrue()
                Log.e("HistoryScreenTest", "User with ID 1 not found in the database.")
            }
        }

        // WHEN: The user navigates to the history screen
        composeTestRule.onNodeWithContentDescription("My Progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.waitForIdle()

        // THEN: The empty state message for the selected date should be displayed
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.no_history_for_date))
            .assertExists()
    }
}
