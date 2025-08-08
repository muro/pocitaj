package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test

class UserProfileTest : BaseExerciseUiTest() {

    @Test
    fun whenProfileIconClicked_thenDropdownMenuAppears() {
        // 1. Click the profile icon
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 2. Verify that the dropdown menu with user options is displayed
        composeTestRule.onNodeWithText("Default User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add User").assertIsDisplayed()
    }
}
