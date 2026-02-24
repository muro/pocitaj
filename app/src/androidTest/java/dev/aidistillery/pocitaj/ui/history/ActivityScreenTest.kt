package dev.aidistillery.pocitaj.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.aidistillery.pocitaj.logic.SmartHighlight
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ActivityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityScreen_showsDataLayerPlaceholders_inPhase1() {
        composeTestRule.setContent {
            AppTheme {
                HistoryScreen(
                    uiState = HistoryUiState(
                        currentStreak = 7,
                        todaysCount = 42,
                        todaysHighlights = listOf(SmartHighlight.SpeedyPaws(3))
                    )
                )
            }
        }

        // Verify placeholders are displayed
        composeTestRule.onNodeWithText("Phase 1: Activity Center Data Layer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Streak: 7").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's Count: 42").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚡ Speedy Paws").assertIsDisplayed()
    }
}
