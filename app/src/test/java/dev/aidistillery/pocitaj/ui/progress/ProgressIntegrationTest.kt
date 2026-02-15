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
        // Start with empty mastery
        val userMastery = mutableMapOf<String, FactMastery>()

        // Real Strategy
        val strategy = TwoDigitDrillStrategy(
            level = level,
            userMastery = userMastery,
            activeUserId = activeUserId
        )

        // ACT
        // Simulate a user solving 30 exercises correctly
        // This exercises the full Component Decomposition logic in recordAttempt
        repeat(30) {
            val exercise = strategy.getNextExercise()
            if (exercise != null) {
                // Determine correct answer
                // Note: TwoDigitEquation.getExpectedResult() is not available on Equation interface directly 
                // without casting or using internal logic, but we can trust the equation object
                // to answer its own question if we had a helper, or just use getExpectedResult defined in Equation interface
                val answer = exercise.equation.getExpectedResult()

                // Solve it (takes 2000ms, plenty for silver badge/progress)
                exercise.solve(answer, 2000)

                // Record it -> This updates userMastery map in place
                strategy.recordAttempt(exercise, wasCorrect = true)
            }
        }

        // Now feed the resulting mastery into the ViewModel
        val mockDao = mockk<FactMasteryDao>()
        // Return the state of mastery AFTER the exercises
        every { mockDao.getAllFactsForUser(activeUserId) } returns flowOf(userMastery.values.toList())

        val viewModel = ProgressReportViewModel(mockDao, activeUserId)

        // ASSERT
        // Wait for the ViewModel to calculate progress
        val progressMap = viewModel.levelProgressByOperation
            .filter { it.isNotEmpty() }
            .first()

        val additionProgress = progressMap[Operation.ADDITION] ?: emptyMap()
        val levelProgress = additionProgress[level.id]?.progress ?: 0f

        // println("Resulting Progress: $levelProgress")

        // We expect valid progress. 
        // 30 correct exercises.
        // Strategy Logic:
        // - It picks 2 weak Ones and 2 weak Tens.
        // - It generates 2x2 = 4 exercises.
        // - Solving these 4 exercises masters the 2 Ones and 2 Tens.
        // - Ratio: 4 exercises -> 4 mastered components.
        // - So we expect approx 1 mastered component per exercise.
        val expectedMastered = 30f
        val totalRequired = level.getAllPossibleFactIds().size // ~91
        val expectedProgress = expectedMastered / totalRequired // ~0.33

        // Allow some tolerance (e.g. +/- 10%) for overlaps or non-ideal repetition
        val tolerance = 0.1f // +/- 10% progress
        
        assertTrue(
            "Progress should be close to $expectedProgress (30 items / $totalRequired), but was $levelProgress",
            levelProgress >= (expectedProgress - tolerance) && levelProgress <= (expectedProgress + tolerance)
        )
    }
}
