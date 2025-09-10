package dev.aidistillery.pocitaj.ui.setup

import app.cash.turbine.test
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FakeActiveUserManager
import dev.aidistillery.pocitaj.data.FakeFactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class ExerciseSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeFactMasteryDao
    private lateinit var viewModel: ExerciseSetupViewModel
    private lateinit var fakeActiveUserManager: FakeActiveUserManager

    @Before
    fun setup() {
        fakeDao = FakeFactMasteryDao()
        fakeActiveUserManager = FakeActiveUserManager()
        viewModel = ExerciseSetupViewModel(fakeDao, fakeActiveUserManager)
    }

    @Test
    fun `operationLevels correctly reflects mastery and unlocked status`() = runTest {
        viewModel.operationLevels.test {
            // 1. Initial state: Should eventually contain the full curriculum
            // The flow initializes with an empty list, so we skip that first emission.
            skipItems(1)

            val initialState = awaitItem()
            val initialAddLevels =
                initialState.find { it.operation == Operation.ADDITION }!!.levelStatuses
            assertEquals(0f, initialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.progress)
            assertTrue(initialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.isUnlocked)
            // TODO: Re-enable and fix these assertions. With the removal of the prerequisite
            // system, these tests need to be rewritten to assert that all levels are unlocked.
            // assertFalse(initialAddLevels.find { it.level.id == "ADD_SUM_10" }!!.isUnlocked)

            // 2. Simulate partial progress
            val allFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds()
            val masteredFacts = allFacts.take(allFacts.size / 2).map {
                FactMastery(it, 1, 2, 0) // Master half the facts to strength 2
            }
            fakeDao.emit(masteredFacts)

            val partialState = awaitItem()
            val partialAddLevels =
                partialState.find { it.operation == Operation.ADDITION }!!.levelStatuses
            val expectedProgress = (allFacts.size / 2 * 2).toFloat() / (allFacts.size * 5)
            assertEquals(
                expectedProgress,
                partialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.progress,
                0.001f
            )


            // 3. Simulate mastering the first level completely
            val allMasteredFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds().map {
                FactMastery(it, 1, 5, 0)
            }
            fakeDao.emit(allMasteredFacts)

            // 4. New state: First level is mastered, second is unlocked
            val updatedState = awaitItem()
            val updatedAddLevels =
                updatedState.find { it.operation == Operation.ADDITION }!!.levelStatuses
            assertEquals(
                1f,
                updatedAddLevels.find { it.level.id == "ADD_SUM_5" }!!.progress,
                0.001f
            )
            assertTrue(updatedAddLevels.find { it.level.id == "ADD_SUM_10" }!!.isUnlocked)
        }
    }
}
