package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Exercise
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileScreenTest : BaseExerciseUiTest() {

    @Test
    fun whenScreenIsLoaded_thenUsersAreDisplayed() {
        // 1. Set up the fake user data
        runBlocking {
            globals.userDao.insert(User(name = "Alice"))
            globals.userDao.insert(User(name = "Bob"))
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
        val userDao = application.globals.userDao
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

    @Test
    fun whenUserIsSwitched_thenProgressIsUpdated() {
        // 1. Set up the fake user data
        runBlocking {
            globals.userDao.insert(User(id = 2, name = "Alice"))
            globals.userDao.insert(User(id = 3, name = "Bob"))
            Curriculum.SubtractionFrom5.getAllPossibleFactIds().forEach { factId ->
                globals.factMasteryDao.upsert(FactMastery(factId, 2, 5, 0))
            }
        }

        // 2. Verify Initial State (Default User)
        openOperationCard("-")
        composeTestRule.onNodeWithTag("${Curriculum.SubtractionFrom5.id}-0_stars")
            .assertIsDisplayed()

        // 3. Switch to Alice
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Alice").performClick()
        composeTestRule.waitForIdle()

        // 4. Verify Alice's State
        openOperationCard("-")
        composeTestRule.onNodeWithTag("user_profile_Alice").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${Curriculum.SubtractionFrom5.id}-3_stars")
            .assertIsDisplayed()

        // 5. Start an Exercise as Alice
        setExercises(listOf(Exercise(Addition(1, 4))))
        navigateToSmartPractice("+")
        drawAnswer("5") // Correct
        verifyFeedback(FeedbackType.CORRECT)
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("Back").performClick()
        composeTestRule.mainClock.advanceTimeBy(RESULT_ANIMATION_PROGRESS_TIME)
        composeTestRule.waitForIdle()

        // 6. Switch to Bob
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        composeTestRule.onNodeWithText("Bob").performClick()
        composeTestRule.waitForIdle()

        // 7. Verify Bob's State
        openOperationCard("-")
        composeTestRule.onNodeWithTag("${Curriculum.SubtractionFrom5.id}-0_stars")
            .assertIsDisplayed()
    }

    @Test
    fun whenUserIsSelected_thenIsSetActive() {
        // 1. Set up the fake user data
        runBlocking {
            globals.userDao.insert(User(id = 2, name = "Alice"))
        }

        // 2. Navigate to the profile screen and select "Alice"
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        composeTestRule.onNodeWithText("Alice").performClick()
        composeTestRule.waitForIdle()

        // 3. Verify that "Alice" is the active user
        assertEquals("Alice", globals.activeUserManager.activeUser.name)
    }

    @Test
    fun whenUserHasCustomAppearance_thenIconAndEditButtonAreDisplayed() {
        // 1. Set up a user with a specific icon and color
        runBlocking {
            globals.userDao.insert(User(name = "Zoe", iconId = "lion", color = 0xFF2196F3.toInt()))
        }

        // 2. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        composeTestRule.waitForIdle()

        // 3. Verify the user's icon is displayed
        // We'll identify the icon by a test tag combining the user's name and iconId
        print(composeTestRule.onRoot().printToString())
        composeTestRule.onNodeWithTag("UserIcon_Zoe_lion", useUnmergedTree = true).assertIsDisplayed()

        // 4. Verify the "Edit" button for that user is displayed
        composeTestRule.onNodeWithContentDescription("Edit Zoe").assertIsDisplayed()
    }

    @Test
    fun whenDefaultUserIsDisplayed_thenEditAndDeleteAreDisabled() {
        // 1. Navigate to the profile screen
        composeTestRule.onNodeWithContentDescription("User Profile")
            .performVerifiedClick("User Profile icon")
        composeTestRule.waitForIdle()

        // 2. Verify the "Edit" button for the default user is not enabled
        composeTestRule.onNodeWithContentDescription("Edit Default User").assertIsNotEnabled()

        // 3. Verify the "Delete" button for the default user is not enabled
        composeTestRule.onNodeWithContentDescription("Delete Default User").assertIsNotEnabled()
    }

    @Test
    fun whenEditIsClicked_thenAppearanceCanBeChanged() {
        // 1. Set up a user
        runBlocking {
            globals.userDao.insert(User(name = "Caleb", iconId = "alligator", color = 0xFFF44336.toInt()))
        }

        // 2. Navigate to the profile screen and click "Edit"
        composeTestRule.onNodeWithContentDescription("User Profile").performClick()
        composeTestRule.onNodeWithContentDescription("Edit Caleb", useUnmergedTree = true).performClick()

        // 3. Verify the dialog appears
        composeTestRule.onNodeWithText("Edit Appearance").assertIsDisplayed()

        // 4. Change the icon, color, and name
        composeTestRule.onNodeWithTag("edit_user_name_field").performTextClearance()
        composeTestRule.onNodeWithTag("edit_user_name_field").performTextInput("Caleb Jr.")
        composeTestRule.onNodeWithTag("icon_select_bull").performClick()
        composeTestRule.onNodeWithTag("color_select_2").performClick() // Index of the purple color
        composeTestRule.onNodeWithText("Save").performClick()

        // 5. Verify the dialog is gone
        composeTestRule.onNodeWithText("Edit Appearance").assertDoesNotExist()

        // 6. Verify the UI has updated with the new appearance
        composeTestRule.onNodeWithText("Caleb Jr.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("UserIcon_Caleb Jr._bull", useUnmergedTree = true).assertIsDisplayed()
        // We would also need a way to verify the color, but checking the icon and name is a strong verification.
    }
}
