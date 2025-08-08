package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.User
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UserProfileScreenTest : BaseExerciseUiTest() {

    @Test
    fun whenScreenIsLoaded_thenUsersAreDisplayed() {
        // 1. Set up the fake user data
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val userDao = application.database.userDao()
        runBlocking {
            userDao.insert(User(name = "Alice"))
            userDao.insert(User(name = "Bob"))
        }

        // 2. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 3. Verify that the users are displayed
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
    }

    @Test
    fun whenProfileIconClicked_thenNavigatesToProfileScreen() {
        // 1. Click the profile icon
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 2. Verify that the UserProfileScreen is displayed
        composeTestRule.onNodeWithText("User Profile").assertIsDisplayed()
    }

    @Test
    fun whenAddUserClicked_thenAddUserDialogIsShown() {
        // 1. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 2. Click the "Add User" button
        composeTestRule.onNodeWithText("Add User")
            .performVerifiedClick("Add User button")

        // 3. Verify that the dialog is shown
        composeTestRule.onNodeWithText("Create a new profile").assertIsDisplayed()
    }

    @Test
    fun whenUserIsAdded_thenAppearsInUserList() {
        // 1. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")

        // 2. Click the "Add User" button
        composeTestRule.onNodeWithText("Add User")
            .performVerifiedClick("Add User button")

        // 3. Enter a new user name and click "Add"
        composeTestRule.onNodeWithText("User Name").performTextInput("Charlie")
        composeTestRule.onNodeWithText("Add").performClick()

        // 4. Verify that the new user is displayed in the list
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
    }

    @Test
    fun whenUserIsDeleted_thenIsRemovedFromUserList() {
        // 1. Set up the fake user data
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val userDao = application.database.userDao()
        runBlocking {
            userDao.insert(User(name = "Alice"))
            userDao.insert(User(name = "Bob"))
        }

        // 2. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        // Verify that "Alice" is displayed
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()


        // 3. Click the delete icon for "Alice"
        composeTestRule.onNodeWithContentDescription("Delete Alice")
            .performVerifiedClick("Delete Alice icon")

        // 4. Verify that "Alice" is no longer displayed
        composeTestRule.onNodeWithText("Alice").assertDoesNotExist()
    }
}
