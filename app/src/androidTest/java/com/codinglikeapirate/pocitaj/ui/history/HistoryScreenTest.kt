package com.codinglikeapirate.pocitaj.ui.history

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codinglikeapirate.pocitaj.BaseExerciseUiTest
import com.codinglikeapirate.pocitaj.TestApp
import com.codinglikeapirate.pocitaj.data.ExerciseAttempt
import com.codinglikeapirate.pocitaj.data.Operation
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest : BaseExerciseUiTest() {

    @Test
    fun historyScreen_displaysHistory() {
        // GIVEN: A set of exercise attempts in the database
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val exerciseAttemptDao = application.database.exerciseAttemptDao()
        val time1 = System.currentTimeMillis()
        val time2 = time1 - 1000 * 60 * 60 * 24 // Yesterday
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
        composeTestRule.onNodeWithText("Progress").performClick()
        composeTestRule.onNodeWithText("View History").performClick()
        composeTestRule.waitForIdle()

        // THEN: The history should be displayed with date headers
        val sdf = android.icu.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date(time1))
        val yesterday = sdf.format(java.util.Date(time2))

        composeTestRule.onNodeWithText(today).assertExists()
        composeTestRule.onNodeWithText("2 + 2 = 4").assertExists()
        composeTestRule.onNodeWithContentDescription("Correct").assertExists()
        composeTestRule.onNodeWithText("1.0s").assertExists()

        composeTestRule.onNodeWithText(yesterday).assertExists()
        composeTestRule.onNodeWithText("3 + 3 = 5").assertExists()
        composeTestRule.onNodeWithContentDescription("Incorrect").assertExists()
        composeTestRule.onNodeWithText("2.1s").assertExists()
    }
}
