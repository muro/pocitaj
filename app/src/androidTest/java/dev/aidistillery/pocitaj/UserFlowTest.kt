package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test

class UserFlowTest : BaseExerciseUiTest() {

    @Test
    fun whenProfileIconClicked_thenNavigatesToProfileScreen() {
        // 1. Click the profile icon
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 2. Verify that the UserProfileScreen is displayed
        composeTestRule.onNodeWithText("User Profiles").assertIsDisplayed()
    }
}
