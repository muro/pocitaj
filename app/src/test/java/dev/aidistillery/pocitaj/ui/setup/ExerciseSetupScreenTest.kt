package dev.aidistillery.pocitaj.ui.setup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.aidistillery.pocitaj.MainActivity
import dev.aidistillery.pocitaj.TestApp
import dev.aidistillery.pocitaj.data.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class, sdk = [34])
class ExerciseSetupScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var viewModel: ExerciseSetupViewModel
    private val activeUserFlow = MutableStateFlow(User(id = 1, name = "John Doe"))

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true) {
            every { activeUser } returns activeUserFlow
        }
    }

    @Test
    fun `when user name is clicked, onProfileClick is called`() {
        composeTestRule.waitUntil(timeoutMillis = 1_000L) {
            composeTestRule.onAllNodesWithText("Choose Your Challenge")
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithText("Choose Your Challenge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default User")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 1_000L) {
            composeTestRule.onAllNodesWithText("User Profile")
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithText("User Profile").assertIsDisplayed()
    }
}
