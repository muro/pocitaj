package dev.aidistillery.pocitaj.data

import androidx.room.Room
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.ui.exercise.ExerciseViewModel
import dev.aidistillery.pocitaj.ui.exercise.UiState
import dev.aidistillery.pocitaj.ui.progress.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AdaptiveExerciseSourceMasteryTest {

    private lateinit var db: AppDatabase
    private lateinit var factMasteryDao: FactMasteryDao
    private lateinit var exerciseAttemptDao: ExerciseAttemptDao
    private lateinit var userDao: UserDao
    private lateinit var activeUserManager: ActiveUserManager
    private lateinit var exerciseSource: AdaptiveExerciseSource
    private lateinit var viewModel: ExerciseViewModel

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        val testDispatcher = dispatcherRule.testDispatcher

        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .build()
        factMasteryDao = db.factMasteryDao()
        exerciseAttemptDao = db.exerciseAttemptDao()
        userDao = db.userDao()

        runTest {
            userDao.insert(User(id = 1, name = "Test User"))
        }

        activeUserManager = FakeActiveUserManager(User(id = 1, name = "Test User"))
        exerciseSource = AdaptiveExerciseSource(factMasteryDao, exerciseAttemptDao, activeUserManager, testDispatcher)
        viewModel = ExerciseViewModel(mockk(relaxed = true), exerciseSource)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `correctly answering an exercise updates mastery in the database`() = runTest {
        // ARRANGE: Start a session for a specific level
        val level = Curriculum.TableLevel(2, Operation.MULTIPLICATION)
        val config = ExerciseConfig(Operation.MULTIPLICATION, 10, 1, level.id)
        viewModel.startExercises(config)
        testScheduler.advanceUntilIdle()

        // ACT: Get the first exercise and answer it correctly via the ViewModel
        val uiState = viewModel.uiState.value
        assertTrue(
            "UI state should be ExerciseScreen, but was $uiState",
            uiState is UiState.ExerciseScreen
        )
        val exercise = (uiState as UiState.ExerciseScreen).currentExercise
        val factId = exercise.getFactId()
        val correctAnswer = exercise.equation.getExpectedResult().toString()

        viewModel.checkAnswer(correctAnswer, 1000)

        testScheduler.advanceUntilIdle()

        // ASSERT: Verify that the database was updated correctly
        val allMastery = factMasteryDao.getAllFactsForUser(1).first()
        val factMasteryEntries = allMastery.filter { it.factId == factId }

        assertTrue("Mastery info should have been saved for the fact", factMasteryEntries.isNotEmpty())
        assertEquals("Two entries should be created (global and per-level)", 2, factMasteryEntries.size)
        
        // Both global and per-level entries should have their strength increased to 4 - the state after answering 1st time correctly
        factMasteryEntries.forEach {
            assertEquals("Strength should be updated to 4", 4, it.strength)
        }
    }
}