package dev.aidistillery.pocitaj.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import dev.aidistillery.pocitaj.ui.theme.Motion
import org.junit.Rule
import org.junit.Test

class ConfettiVisibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confetti_is_visible_during_animation_and_disappears_after_duration() {
        val confettiDuration = Motion().confetti.toLong()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            AppTheme {
                ConfettiAnimation(
                    particleCount = 10
                )
            }
        }

        // 1. Initial state (0ms) - should be present
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 2. Midpoint - should still be present
        composeTestRule.mainClock.advanceTimeBy(confettiDuration / 2)
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 3. Just before the end - should still be present
        composeTestRule.mainClock.advanceTimeBy(confettiDuration / 3)
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 4. At the end - it's removed by the if (progress.value < 1f) logic
        // We advance a tiny bit more to ensure the state update is processed
        composeTestRule.mainClock.advanceTimeBy(confettiDuration / 6 + 100)

        // Use assertDoesNotExist to verify it's been removed from the tree
        composeTestRule.onNodeWithTag("confetti_animation").assertDoesNotExist()
    }
}
