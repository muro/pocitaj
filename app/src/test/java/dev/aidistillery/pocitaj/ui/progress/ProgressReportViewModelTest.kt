package dev.aidistillery.pocitaj.ui.progress

import app.cash.turbine.test
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Curriculum.SumsUpTo5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

const val ENABLE_DETAILED_LOGGING = false

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
    fun `flows emit correct progress data`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao)
        val facts = listOf(
            FactMastery("ADDITION_1_1", 1, 5, 5), // Mastered
            FactMastery("MULTIPLICATION_2_3", 1, 3, 4) // Learning
        )

        // ACT & ASSERT for factProgressByOperation
        viewModel.factProgressByOperation.test {
            // Initial state check
            val initialFactProgress = awaitItem()
            assertEquals(4, initialFactProgress.size)
            assertTrue(initialFactProgress.values.all { it.isEmpty() })

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedFactProgress = awaitItem()
            assertEquals(4, updatedFactProgress.size)

            val additionFacts = updatedFactProgress[Operation.MULTIPLICATION]
            assertNotNull(additionFacts)
            if (ENABLE_DETAILED_LOGGING) {
                for (f in additionFacts!!) {
                    println(f)
                }
            }
            assertEquals(121, additionFacts!!.size)
            val masteredFact = additionFacts.find { it.factId == "MULTIPLICATION_2_3" }
            assertNotNull(masteredFact)
            assertEquals(3, masteredFact!!.mastery!!.strength)
        }

        // ACT & ASSERT for levelProgressByOperation
        viewModel.levelProgressByOperation.test {
            // Initial state check
            val initialLevelProgress = awaitItem()
            assertEquals(0, initialLevelProgress.size)

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedLevelProgress = awaitItem()
            assertEquals(4, updatedLevelProgress.size)

            val additionLevels = updatedLevelProgress[Operation.ADDITION]
            assertNotNull(additionLevels)
            val sumsUpTo5Progress = additionLevels!![SumsUpTo5.id]
            assertNotNull(sumsUpTo5Progress)
            assertTrue(sumsUpTo5Progress!!.progress > 0)
        }
    }

    @Test
    fun `level progress`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao)
        val facts = listOf(
            FactMastery("ADDITION_1_1", 1, 5, 5), // Mastered
            FactMastery("ADDITION_1_3", 1, 5, 5), // Mastered
            FactMastery("ADDITION_3_1", 1, 5, 5), // Mastered
        )

        // ACT & ASSERT for levelProgressByOperation
        viewModel.levelProgressByOperation.test {
            // Initial state check
            val initialLevelProgress = awaitItem()
            assertEquals(0, initialLevelProgress.size)

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedLevelProgress = awaitItem()
            assertEquals(4, updatedLevelProgress.size)

            val additionLevels = updatedLevelProgress[Operation.ADDITION]!!
            assertNotNull(additionLevels)

            assertTrue(additionLevels.containsKey("ADD_SUM_5"))
            val s5 = additionLevels["ADD_SUM_5"]!!
            assertTrue(3 / 25 <= s5.progress)
            assertTrue(additionLevels.containsKey("ADD_SUM_10"))
            val s10 = additionLevels["ADD_SUM_10"]!!
            assertTrue(0.03 <= s10.progress)
            assertTrue(additionLevels.containsKey("ADD_TWO_DIGIT_CARRY"))
            assertEquals(0.0f, additionLevels["ADD_TWO_DIGIT_CARRY"]!!.progress)
        }
    }

    @Test
    fun `levelProgress_updatesForHalfAndFullMastery`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao)
        val masteredAdditionFacts = SumsUpTo5.getAllPossibleFactIds().map {
            FactMastery(it, 1, 5, 0)
        }
        val subtractionFacts = Curriculum.SubtractionFrom5.getAllPossibleFactIds()
        val masteredSubtractionFacts = subtractionFacts.take(subtractionFacts.size / 2).map {
            FactMastery(it, 1, 5, 0)
        }
        val facts = masteredAdditionFacts + masteredSubtractionFacts

        // ACT & ASSERT
        viewModel.levelProgressByOperation.test {
            skipItems(1) // Skip initial empty state

            fakeDao.emit(facts)

            val progress = awaitItem()
            val additionProgress = progress[Operation.ADDITION]!![SumsUpTo5.id]!!
            val subtractionProgress =
                progress[Operation.SUBTRACTION]!![Curriculum.SubtractionFrom5.id]!!

            assertEquals(1.0f, additionProgress.progress)
            assertTrue(additionProgress.isMastered)
            assertEquals(0.5f, subtractionProgress.progress, 0.05f)
        }
    }
}
