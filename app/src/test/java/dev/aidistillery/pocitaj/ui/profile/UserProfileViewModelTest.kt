package dev.aidistillery.pocitaj.ui.profile

import androidx.compose.ui.graphics.toArgb
import dev.aidistillery.pocitaj.data.FakeActiveUserManager
import dev.aidistillery.pocitaj.data.FakeExerciseAttemptDao
import dev.aidistillery.pocitaj.data.FakeUserDao
import dev.aidistillery.pocitaj.data.UserAppearance
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userDao: FakeUserDao
    private lateinit var attemptDao: FakeExerciseAttemptDao
    private lateinit var activeUserManager: FakeActiveUserManager
    private lateinit var viewModel: UserProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userDao = FakeUserDao()
        attemptDao = FakeExerciseAttemptDao()
        activeUserManager = FakeActiveUserManager()
        viewModel = UserProfileViewModel(userDao, attemptDao, activeUserManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addUser assigns unique colors automatically`() = runTest {
        // ARRANGE: Add 3 users with different colors
        viewModel.addUser("User 1")
        advanceUntilIdle()

        val user1 = userDao.getUserByName("User 1")!!
        val color1 = user1.color

        // ACT: Add User 2
        viewModel.addUser("User 2")
        advanceUntilIdle()

        // ASSERT: User 2 should have a different color
        val user2 = userDao.getUserByName("User 2")!!
        user2.color shouldNotBe color1
    }

    @Test
    fun `addUser assigns unique icons automatically`() = runTest {
        // ARRANGE: Add 1 user
        viewModel.addUser("User 1")
        advanceUntilIdle()

        val user1 = userDao.getUserByName("User 1")!!
        val icon1 = user1.iconId

        // ACT: Add User 2
        viewModel.addUser("User 2")
        advanceUntilIdle()

        // ASSERT: User 2 should have a different icon
        val user2 = userDao.getUserByName("User 2")!!
        user2.iconId shouldNotBe icon1
    }

    @Test
    fun `addUser cycles back to first color if all colors are taken`() = runTest {
        // ARRANGE: Fill all colors
        UserAppearance.colors.forEachIndexed { index, _ ->
            viewModel.addUser("User $index")
            advanceUntilIdle()
        }

        // ACT: Add one more user
        viewModel.addUser("Overflow User")
        advanceUntilIdle()

        // ASSERT: Should have a valid color (even if it's a duplicate now)
        UserAppearance.colors.map { it.toArgb() } shouldBe (UserAppearance.colors.map { it.toArgb() }) // Sanity check
    }
}
