package dev.aidistillery.pocitaj.ui.progress

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.TwoDigitDrillStrategy
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressIntegrationTest {

    @Test
    fun `solving exercises naturally increases level progress`() = runBlocking {
        // ARRANGE
        val activeUserId = 1L
        val level = Curriculum.TwoDigitAdditionNoCarry
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = TwoDigitDrillStrategy(level, userMastery, activeUserId = activeUserId)

        // ACT: Simulate user solving 30 exercises
        repeat(30) {
            val exercise = strategy.getNextExercise() ?: return@repeat
            exercise.solve(exercise.equation.getExpectedResult(), 2000)
            strategy.recordAttempt(exercise, wasCorrect = true)
        }

        // Setup ViewModel AFTER exercises are solved so it captures the completed state
        val mockDao = mockk<FactMasteryDao> {
            every { getAllFactsForUser(activeUserId) } returns flowOf(userMastery.values.toList())
        }
        val viewModel = ProgressReportViewModel(mockDao, activeUserId)

        // ASSERT
        val progressMap = viewModel.levelProgressByOperation.filter { it.isNotEmpty() }.first()
        val levelProgress = progressMap[Operation.ADDITION]?.get(level.id)?.progress ?: 0f

        // Strategy picks 2 weak Ones + 2 weak Tens -> generates 4 exercises.
        // Each wave of 4 exercises masters those 4 components. Ratio is ~1.0.
        val totalFacts = level.getAllPossibleFactIds().size
        val expectedProgress = 30f / totalFacts
        val tolerance = 0.1f

        assertTrue(
            "Progress $levelProgress should be close to 30/$totalFacts",
            levelProgress in (expectedProgress - tolerance)..(expectedProgress + tolerance)
        )
    }
}
