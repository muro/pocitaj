package dev.aidistillery.pocitaj.ui.history

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ActivityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityScreen_showsHeatmapAndHistory() {
        val today = LocalDate.now()
        val attempts = listOf(
            ExerciseAttempt(
                userId = 1,
                timestamp = System.currentTimeMillis(),
                problemText = "2 + 2 = ?",
                logicalOperation = Operation.ADDITION,
                correctAnswer = 4,
                submittedAnswer = 4,
                wasCorrect = true,
                durationMs = 1000
            )
        )

        val dailyActivity = mapOf(today to 1)

        composeTestRule.setContent {
            AppTheme {
                HistoryScreen(
                    uiState = HistoryUiState(
                        dailyActivity = dailyActivity,
                        selectedDate = today,
                        filteredHistory = attempts
                    ),
                    onDateSelected = {}
                )
            }
        }

        // Verify heatmap is displayed
        composeTestRule.onNodeWithTag("activity_heatmap").assertIsDisplayed()

        // Verify history item for the selected day is shown
        composeTestRule.onNodeWithText("2 + 2 = 4").assertIsDisplayed()
    }

    @Test
    fun tappingDay_updatesSelection() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        var selectedDateResult: LocalDate? = null

        composeTestRule.setContent {
            AppTheme {
                ActivityHeatmap(
                    dailyActivity = mapOf(today to 5, yesterday to 15),
                    selectedDate = today,
                    onDateSelected = { selectedDateResult = it }
                )
            }
        }

        // Verify it exists and is clickable
        composeTestRule.onNodeWithTag("heatmap_day_$yesterday")
            .assertExists()
            .assertHasClickAction()
            .performClick()

        composeTestRule.waitForIdle()

        if (selectedDateResult == null) {
            // If still null, try one more time with explicit touch input
            composeTestRule.onNodeWithTag("heatmap_day_$yesterday")
                .performTouchInput { click() }
            composeTestRule.waitForIdle()
        }

        selectedDateResult shouldBe yesterday
    }
}
