package com.codinglikeapirate.pocitaj.ui.history

import android.util.Log
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.codinglikeapirate.pocitaj.BaseExerciseUiTest
import com.codinglikeapirate.pocitaj.R
import com.codinglikeapirate.pocitaj.TestApp
import com.codinglikeapirate.pocitaj.data.ExerciseAttempt
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.data.User
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class HistoryScreenTest : BaseExerciseUiTest() {

    @Before
    fun setup() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        // TODO: Hack, clean up. Can't make it reliably work, no idea where the db gets cleaned up.
        runBlocking {
             if (application.database.userDao().getUser(1) == null) {
                 application.database.userDao().insert(User(id = 1, name = "Default User"))
             }
        }
    }

    @Test
    fun historyScreen_displaysHistory() {
        // GIVEN: A set of exercise attempts in the database
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val exerciseAttemptDao = application.database.exerciseAttemptDao()

        runBlocking {
            if (application.database.userDao().getUser(1) == null) {
                assertTrue(false)
                Log.e("HistoryScreenTest", "User with ID 1 not found in the database.")
            }
        }
        val time1 = 1678901460000L // March 15, 2023 15:31:00 UTC
        val time2 = 1678864920000L // March 15, 2023 09:22:00 UTC
        runBlocking {
            exerciseAttemptDao.insert(
                ExerciseAttempt(
                    problemText = "2 + 2",
                    submittedAnswer = 4,
                    wasCorrect = true,
                    correctAnswer = 4,
                    durationMs = 1000,
                    timestamp = time1,
                    userId = 1,
                    logicalOperation = Operation.ADDITION
                )
            )
            exerciseAttemptDao.insert(
                ExerciseAttempt(
                    problemText = "3 + 3",
                    submittedAnswer = 5,
                    wasCorrect = false,
                    correctAnswer = 6,
                    durationMs = 2100,
                    timestamp = time2,
                    userId = 1,
                    logicalOperation = Operation.ADDITION
                )
            )
        }

        // WHEN: The user navigates to the history screen
        composeTestRule.onNodeWithText("My Progress").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.waitForIdle()

        // THEN: The history should be displayed with date headers
        val sdf = android.icu.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date(time1))
        val yesterday = sdf.format(java.util.Date(time2))

        composeTestRule.onNodeWithText(today).assertExists()
        composeTestRule.onNodeWithText("2 + 2 = 4").assertExists()
        composeTestRule.onNodeWithContentDescription("Correct").assertExists()
        val timeFormat = android.icu.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        val time1String = timeFormat.format(java.util.Date(time1))
        composeTestRule.onNodeWithText(time1String).assertExists()
        composeTestRule.onNodeWithText("1.0s").assertExists()

        composeTestRule.onNodeWithText(yesterday).assertExists()
        composeTestRule.onNodeWithText("3 + 3 = 5").assertExists()
        composeTestRule.onNodeWithContentDescription("Incorrect").assertExists()
        val time2String = timeFormat.format(java.util.Date(time2))
        composeTestRule.onNodeWithText(time2String).assertExists()
        composeTestRule.onNodeWithText("2.1s").assertExists()
    }

    @Test
    fun historyScreen_displaysEmptyState() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        runBlocking {
            if (application.database.userDao().getUser(1) == null) {
                assertTrue(false)
                Log.e("HistoryScreenTest", "User with ID 1 not found in the database.")
            }
        }


        // WHEN: The user navigates to the history screen
        composeTestRule.onNodeWithText("My Progress").performClick()
        composeTestRule.waitForIdle()
        // Can't make the swipes to work in a test
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.waitForIdle()

        // THEN: The empty state message should be displayed
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.no_history_yet)).assertExists()
    }
}
