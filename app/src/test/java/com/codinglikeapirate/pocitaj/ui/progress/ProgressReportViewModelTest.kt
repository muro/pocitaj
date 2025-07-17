package com.codinglikeapirate.pocitaj.ui.progress

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import app.cash.turbine.test
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


// This rule replaces the main dispatcher with a test dispatcher
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class ProgressReportViewModelTest {

    // 1. Use the MainDispatcherRule to control the Main dispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Create a fake DAO with a controllable flow
    class FakeFactMasteryDao : FactMasteryDao {
        private val flow = MutableStateFlow<List<FactMastery>>(emptyList())

        // This function lets our test push new values to the flow
        suspend fun emit(value: List<FactMastery>) {
            flow.emit(value)
        }

        override fun getAllFactsForUser(userId: Long): Flow<List<FactMastery>> = flow
        override suspend fun getFactMastery(
            userId: Long,
            factId: String
        ): FactMastery? {
            TODO("Not yet implemented")
        }

        override suspend fun upsert(factMastery: FactMastery) {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `itemCount correctly reflects number of items from dao`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao)

        // ACT & ASSERT using Turbine
        viewModel.operationProgress.test {
            // StateFlow<Map<Operation, List<FactProgress>>>
            // Check the initial state
            assertEquals(0, awaitItem().size)

            // Push a new list from the fake DAO
            // List<FactMastery>
            fakeDao.emit(listOf(
                FactMastery("ADDITION_1_1", 1, 5, 5),
                FactMastery("ADDITION_1_2", 1, 5, 5),
                FactMastery("ADDITION_1_3", 1, 5, 5),
                FactMastery("ADDITION_1_4", 1, 5, 5),
                FactMastery("ADDITION_1_5", 1, 5, 5),
                FactMastery("ADDITION_1_6", 1, 5, 5),
                FactMastery("ADDITION_1_7", 1, 5, 5),
                FactMastery("ADDITION_1_8", 1, 5, 5),
                FactMastery("ADDITION_1_9", 1, 5, 5),
                FactMastery("ADDITION_1_10", 1, 5, 5),
                FactMastery("ADDITION_1_11", 1, 5, 5),

                FactMastery("MULTIPLICATION_1_1", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_2", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_3", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_4", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_5", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_6", 1, 5, 3),
                FactMastery("MULTIPLICATION_1_3", 1, 5, 3)))
            val operationProgress = awaitItem()
            assertEquals(4, operationProgress.size)
            val additionProgress = operationProgress[Operation.ADDITION]!!.progress
            assertTrue("Expected 0.08 <= $additionProgress <= 0.09",
                additionProgress >= 0.08 && additionProgress <= 0.09)
            assertFalse(operationProgress[Operation.ADDITION]!!.isMastered)
            val multiplicationProgress = operationProgress[Operation.MULTIPLICATION]!!.progress
            assertTrue("Expected 0.03 <= $multiplicationProgress <= 0.04",
                multiplicationProgress >= 0.03 && multiplicationProgress <= 0.04)
            assertFalse(operationProgress[Operation.MULTIPLICATION]!!.isMastered)
        }
    }
}