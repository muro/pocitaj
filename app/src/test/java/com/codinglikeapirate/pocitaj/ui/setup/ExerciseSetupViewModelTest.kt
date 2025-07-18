package com.codinglikeapirate.pocitaj.ui.setup

import app.cash.turbine.test
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    class FakeFactMasteryDao : FactMasteryDao {
        private val flow = MutableStateFlow<List<FactMastery>>(emptyList())
        suspend fun emit(value: List<FactMastery>) {
            flow.emit(value)
        }
        override fun getAllFactsForUser(userId: Long) = flow.asStateFlow()
        override suspend fun getFactMastery(userId: Long, factId: String): FactMastery? = null
        override suspend fun upsert(factMastery: FactMastery) {}
    }

    @Before
    fun setup() {
        fakeDao = FakeFactMasteryDao()
        viewModel = ExerciseSetupViewModel(fakeDao)
    }

    @Test
    fun `operationLevels correctly reflects mastery and unlocked status`() = runTest {
        viewModel.operationLevels.test {
            // 1. Initial state: Should eventually contain the full curriculum
            // The flow initializes with an empty list, so we skip that first emission.
            skipItems(1)

            val initialState = awaitItem()
            val initialAddLevels = initialState.find { it.operation == Operation.ADDITION }!!.levels
            assertEquals(0, initialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.starRating)
            assertTrue(initialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.isUnlocked)
            assertFalse(initialAddLevels.find { it.level.id == "ADD_SUM_10" }!!.isUnlocked)

            // 2. Simulate mastering the first level
            val masteredFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds().map {
                FactMastery(it, 1, 5, 0)
            }
            fakeDao.emit(masteredFacts)

            // 3. New state: First level is mastered, second is unlocked
            val updatedState = awaitItem()
            val updatedAddLevels = updatedState.find { it.operation == Operation.ADDITION }!!.levels
            assertEquals(3, updatedAddLevels.find { it.level.id == "ADD_SUM_5" }!!.starRating)
            assertTrue(updatedAddLevels.find { it.level.id == "ADD_SUM_10" }!!.isUnlocked)
            assertFalse(updatedAddLevels.find { it.level.id == "ADD_SUM_20" }!!.isUnlocked)
        }
    }
}
