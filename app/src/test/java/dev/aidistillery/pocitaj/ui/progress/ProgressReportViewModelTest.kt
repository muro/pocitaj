package dev.aidistillery.pocitaj.ui.progress

import app.cash.turbine.test
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FakeFactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

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

    @Test
    fun `flows emit correct progress data`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao, 1)
        val facts = listOf(
            FactMastery("1 + 1 = ?", 1, Curriculum.SumsUpTo5.id, 5, 5), // Mastered
            FactMastery(
                "2 * 3 = ?",
                1,
                Curriculum.TableLevel(Operation.MULTIPLICATION, 3).id,
                3,
                4
            ) // Learning
        )

        // ACT & ASSERT for factProgressByOperation
        viewModel.factProgressByOperation.test {
            // Initial state check
            val initialFactProgress = awaitItem()
            initialFactProgress.size shouldBe 4
            initialFactProgress.values.all { it.isEmpty() }.shouldBeTrue()

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedFactProgress = awaitItem()
            updatedFactProgress.size shouldBe 4

            val additionFacts = updatedFactProgress[Operation.MULTIPLICATION]
            additionFacts.shouldNotBeNull()
            additionFacts.size shouldBe 121
            val masteredFact = additionFacts.find { it.factId == "2 * 3 = ?" }
            masteredFact.shouldNotBeNull()
            masteredFact.mastery!!.strength shouldBe 3
        }

        // ACT & ASSERT for levelProgressByOperation
        viewModel.levelProgressByOperation.test {
            // Initial state check
            val initialLevelProgress = awaitItem()
            initialLevelProgress.size shouldBe 0

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedLevelProgress = awaitItem()
            updatedLevelProgress.size shouldBe 4

            val additionLevels = updatedLevelProgress[Operation.ADDITION]
            additionLevels.shouldNotBeNull()
            val sumsUpTo5Progress = additionLevels[Curriculum.SumsUpTo5.id]
            sumsUpTo5Progress.shouldNotBeNull()
            (sumsUpTo5Progress.progress > 0).shouldBeTrue()
        }
    }

    @Test
    fun `level progress`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao, 1)
        val facts = listOf(
            FactMastery("1 + 1 = ?", 1, Curriculum.SumsUpTo5.id, 5, 5), // Mastered
            FactMastery("1 + 3 = ?", 1, Curriculum.SumsUpTo5.id, 5, 5), // Mastered
            FactMastery("3 + 1 = ?", 1, Curriculum.SumsUpTo5.id, 5, 5), // Mastered
            FactMastery(
                "3 + 3 = ?",
                1,
                Curriculum.SumsUpTo10.id,
                5,
                5
            ), // Mastered (Sum 6 -> SumsUpTo10)
            FactMastery(
                "4 + 4 = ?",
                1,
                Curriculum.SumsUpTo10.id,
                5,
                5
            ), // Mastered (Sum 8 -> SumsUpTo10)
        )

        // ACT & ASSERT for levelProgressByOperation
        viewModel.levelProgressByOperation.test {
            // Initial state check
            val initialLevelProgress = awaitItem()
            initialLevelProgress.size shouldBe 0

            // State after emitting facts
            fakeDao.emit(facts)

            val updatedLevelProgress = awaitItem()
            updatedLevelProgress.size shouldBe 4

            val additionLevels = updatedLevelProgress[Operation.ADDITION]!!
            additionLevels.shouldNotBeNull()

            additionLevels.shouldContainKey("ADD_SUM_5")
            val s5 = additionLevels["ADD_SUM_5"]!!
            (s5.progress >= 3f / 25f).shouldBeTrue()

            additionLevels.shouldContainKey("ADD_SUM_10")
            val s10 = additionLevels["ADD_SUM_10"]!!
            (s10.progress >= 0.03f).shouldBeTrue()

            additionLevels.shouldContainKey("ADD_TWO_DIGIT_CARRY")
            additionLevels["ADD_TWO_DIGIT_CARRY"]!!.progress shouldBe 0.0f
        }
    }

    @Test
    fun `level progress updates for half and full mastery`() = runTest {
        // ARRANGE
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao, 1)
        val masteredAdditionFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds().map {
            FactMastery(it, 1, Curriculum.SumsUpTo5.id, 5, 0)
        }
        val subtractionFacts = Curriculum.SubtractionFrom5.getAllPossibleFactIds()
        val masteredSubtractionFacts = subtractionFacts.take(subtractionFacts.size / 2).map {
            FactMastery(it, 1, Curriculum.SubtractionFrom5.id, 5, 0)
        }
        val facts = masteredAdditionFacts + masteredSubtractionFacts

        // ACT & ASSERT
        viewModel.levelProgressByOperation.test {
            skipItems(1) // Skip initial empty state

            fakeDao.emit(facts)

            val progress = awaitItem()
            val additionProgress = progress[Operation.ADDITION]!![Curriculum.SumsUpTo5.id]!!
            val subtractionProgress =
                progress[Operation.SUBTRACTION]!![Curriculum.SubtractionFrom5.id]!!

            additionProgress.progress shouldBe 1.0f
            additionProgress.isMastered.shouldBeTrue()
            subtractionProgress.progress shouldBe (0.5f plusOrMinus 0.05f)
        }
    }

    @Test
    fun `level progress is isolated between levels`() = runTest {
        val fakeDao = FakeFactMasteryDao()
        val viewModel = ProgressReportViewModel(fakeDao, 1)
        val doublesId = Curriculum.Doubles.id
        val sumsUpTo5Id = Curriculum.SumsUpTo5.id

        // Master 2+2 in Doubles only
        val facts = listOf(
            FactMastery("2 + 2 = ?", 1, doublesId, 5, 0)
        )

        viewModel.levelProgressByOperation.test {
            skipItems(1) // Initial
            fakeDao.emit(facts)

            val progress = awaitItem()
            val additionProgress = progress[Operation.ADDITION]!!

            // Doubles should have progress (2+2 is in Doubles)
            val doublesProgress = additionProgress[doublesId]
            doublesProgress.shouldNotBeNull()
            (doublesProgress.progress > 0f).shouldBeTrue()

            // SumsUpTo5 should NOT have progress (2+2 is also in SumsUpTo5, but mastery was for Doubles)
            val sumsProgress = additionProgress[sumsUpTo5Id]
            sumsProgress.shouldNotBeNull()
            sumsProgress.progress shouldBe 0f
        }
    }
}
