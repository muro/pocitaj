package dev.aidistillery.pocitaj.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class ConfettiVisibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confetti_is_visible_during_animation_and_disappears_after_duration() {
        // Set a short duration for testing
        val duration = 1000

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            AppTheme {
                ConfettiAnimation(
                    durationMillis = duration,
                    particleCount = 10
                )
            }
        }

        // 1. Initial state (0ms) - should be present
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 2. Midpoint (500ms) - should still be present
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 3. Just before the end (900ms) - should still be present
        composeTestRule.mainClock.advanceTimeBy(400)
        composeTestRule.onNodeWithTag("confetti_animation").assertExists()

        // 4. At the end (1000ms) - it's removed by the if (progress.value < 1f) logic
        // We advance a tiny bit more to ensure the state update is processed
        composeTestRule.mainClock.advanceTimeBy(100)

        // Use assertDoesNotExist to verify it's been removed from the tree
        composeTestRule.onNodeWithTag("confetti_animation").assertDoesNotExist()
    }
}
