package dev.aidistillery.pocitaj.ui.credits

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.aidistillery.pocitaj.BaseExerciseUiTest
import org.junit.Test

class CreditsScreenTest : BaseExerciseUiTest() {

    @Test
    fun creditsScreen_isDisplayed_andLoadsLibraries() {
        // 1. Navigate to the CreditsScreen
        composeTestRule.onNodeWithContentDescription("Credits").performClick()
        composeTestRule.waitForIdle()

        // 2. Verify that the static title is displayed immediately
        composeTestRule.onNodeWithText("Open Source Licenses").assertIsDisplayed()

        // 3. Wait until the asynchronously loaded library list appears
        composeTestRule.onAllNodesWithText("AboutLibraries", substring = true).fetchSemanticsNodes().isNotEmpty()

        // 4. Find the scrollable container and scroll to the specific library
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Baloo 2 Font"))

        // 5. Verify that the specific library is now displayed
        composeTestRule.onNodeWithText("Baloo 2 Font").assertIsDisplayed()
    }
}
