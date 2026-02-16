package dev.aidistillery.pocitaj.ui.setup

import app.cash.turbine.test
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FakeActiveUserManager
import dev.aidistillery.pocitaj.data.FakeFactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
            val sum5Level = initialAddLevels.find { it.level.id == "ADD_SUM_5" }!!
            sum5Level.progress shouldBe 0f
            sum5Level.isUnlocked.shouldBeTrue()

            val sum10Level = initialAddLevels.find { it.level.id == "ADD_SUM_10" }!!
            // SUM_10 is locked initially (prereqs not met)
            sum10Level.isUnlocked.shouldBeFalse()

            // 2. Simulate partial progress
            val allFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds()
            val masteredFacts = allFacts.take(allFacts.size / 2).map {
                FactMastery(it, 1, "", 2, 0) // Master half the facts to strength 2
            }
            // State after emitting facts
            fakeDao.emit(masteredFacts)

            val partialState = awaitItem()
            val partialAddLevels =
                partialState.find { it.operation == Operation.ADDITION }!!.levelStatuses

            // New math: strength 2 has weight 0.1. 
            // SumsUpTo5 has 21 facts. masteredFacts takes 10 of them.
            // (10 * 0.1 + 11 * 0.0) / 21 = 1.0 / 21 = 0.0476...
            val expectedProgress = 1.0f / 21f
            partialAddLevels.find { it.level.id == "ADD_SUM_5" }!!.progress shouldBe (expectedProgress plusOrMinus 0.001f)


            // 3. Simulate mastering the first level completely
            val allMasteredFacts = Curriculum.SumsUpTo5.getAllPossibleFactIds().map {
                FactMastery(it, 1, "", 5, 0)
            }
            fakeDao.emit(allMasteredFacts)

            // 4. New state: First level is mastered, second is unlocked
            val updatedState = awaitItem()
            val updatedAddLevels =
                updatedState.find { it.operation == Operation.ADDITION }!!.levelStatuses

            updatedAddLevels.find { it.level.id == "ADD_SUM_5" }!!.progress shouldBe (1f plusOrMinus 0.001f)
            updatedAddLevels.find { it.level.id == "ADD_SUM_10" }!!.isUnlocked.shouldBeTrue()
        }
    }
}
