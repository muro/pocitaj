package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserDao
import dev.aidistillery.pocitaj.ui.profile.UserProfileViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UserProfileScreenTest : BaseExerciseUiTest() {

    class FakeUserDao : UserDao {
        private val users = mutableMapOf<Long, User>()
        private var nextId = 1L
        override suspend fun insert(user: User): Long {
            val idToInsert = user.id.takeIf { it != 0L } ?: nextId++
            users[idToInsert] = user.copy(id = idToInsert)
            return idToInsert
        }

        override fun getAllUsers(): Flow<List<User>> {
            return MutableStateFlow(users.values.toList())
        }

        override suspend fun getUser(id: Long): User? = users[id]
        override suspend fun getUserByName(name: String): User? =
            users.values.find { it.name == name }
    }

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
        composeTestRule.onNodeWithText("User Profiles").assertIsDisplayed()
    }
}
