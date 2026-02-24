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

        // Verify Phase 2 components are displayed
        composeTestRule.onNodeWithText("🔥 7 Day Streak!").assertIsDisplayed()
        composeTestRule.onNodeWithText("42 exercises completed today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Speedy Paws").assertIsDisplayed()
    }
}
