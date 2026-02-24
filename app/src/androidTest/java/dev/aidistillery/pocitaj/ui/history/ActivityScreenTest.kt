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

        composeTestRule.onNodeWithText("🔥").assertIsDisplayed()
        composeTestRule.onNodeWithText("7 Day Streak!").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 Rewards earned! Let's get the last one!")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Speedy Paws").assertIsDisplayed()
    }
}
