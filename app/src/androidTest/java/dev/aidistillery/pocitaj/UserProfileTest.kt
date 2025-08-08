package dev.aidistillery.pocitaj

import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Test

class UserProfileTest : BaseExerciseUiTest() {

    @Test
    fun whenProfileIconClicked_thenSomethingHappens() {
        // For now, just verify that the icon is clickable.
        // In the future, this test will be expanded to verify that a dropdown menu appears.
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
    }
}
