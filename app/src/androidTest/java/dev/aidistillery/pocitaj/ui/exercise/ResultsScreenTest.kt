package dev.aidistillery.pocitaj.ui.exercise

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.data.SessionResult
import dev.aidistillery.pocitaj.data.StarProgress
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ResultsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()


    @Test
    fun confetti_displayed_when_perfect_score() {
        // 100% correct
        val results = List(10) {
            ResultDescription("2+2=4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD)
        }

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AppTheme {
                ResultsScreen(
                    sessionResult = SessionResult(results, StarProgress(0, 1)),
                    onDone = {},
                    onDoAgain = {},
                    onProgressClicked = {},
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500) // Advance enough to start animation and ensuring drawing
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()
        composeTestRule.onNodeWithTag("confetti_animation").assertIsDisplayed()
    }

    @Test
    fun confetti_not_displayed_when_imperfect_score() {
        // 90% correct, which is high but not perfect
        val results = List(9) {
            ResultDescription("2+2=4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD)
        } + listOf(
            ResultDescription("1+1=3", ResultStatus.INCORRECT, 1000, SpeedBadge.NONE)
        )

        composeTestRule.setContent {
            AppTheme {
                ResultsScreen(
                    sessionResult = SessionResult(results, StarProgress(1, 1)),
                    onDone = {},
                    onDoAgain = {},
                    onProgressClicked = {}
                )
            }
        }

        composeTestRule.onAllNodesWithTag("confetti_animation").assertCountEquals(0)
    }


    @Test
    fun when_star_gained_celebration_is_shown() {
        // ARRANGE: A session where the user gains a star (1 -> 2)
        val results = List(5) {
            ResultDescription("2+2=4", ResultStatus.CORRECT, 1000, SpeedBadge.GOLD)
        }

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            AppTheme {
                ResultsScreen(
                    sessionResult = SessionResult(results, StarProgress(1, 2)),
                    onDone = {},
                    onDoAgain = {},
                    onProgressClicked = {}
                )
            }
        }

        // ASSERT: Verify confetti animation exists
        // It should be present immediately as it's forced in DEBUG mode
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()
    }
}
