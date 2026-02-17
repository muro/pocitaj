package dev.aidistillery.pocitaj.ui.exercise

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class ResultsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
                    results = results,
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
                    results = results,
                    onDone = {},
                    onDoAgain = {},
                    onProgressClicked = {}
                )
            }
        }

        composeTestRule.onAllNodesWithTag("confetti_animation").assertCountEquals(0)
    }


}
