package dev.aidistillery.pocitaj.ui.progress

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.ExerciseStrategy
import dev.aidistillery.pocitaj.logic.Level
import dev.aidistillery.pocitaj.logic.TwoDigitComputationLevel
import dev.aidistillery.pocitaj.logic.createStrategy
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ProgressIntegrationTest(private val level: Level) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Level> {
            return Curriculum.getAllLevels()
        }
    }

    @Test
    fun `solving exercises increases level progress correctly`() = runBlocking {
        val activeUserId = 1L
        val userMastery = mutableMapOf<String, FactMastery>()

        // Use the factory to get the correct strategy for the level
        val strategy = level.createStrategy(userMastery, activeUserId)

        val allFactIds = level.getAllPossibleFactIds()
        val totalFacts = allFactIds.size

        // Target ~50% progress.
        // - Baseline: Starting strength is 3. GOLD (+1) needs 2 hits to reach 5.
        // - Standard Drill: masters 1 fact per 2 exercises (on average). 
        //   First N exercises bring all to Strength 4. Next N/2 bring half to Strength 5.
        //   So 1.5 * N exercises = 50% progress.
        // - Two-Digit Drill: 1 ex = 2 component hits. Each component needs 2 hits.
        //   So 1 exercise = 1 mastered component. N/2 ex = 50% progress.
        // - Review: 1 ex = 1 mastered fact (promotes instantly to 6). So N/2 exercises = 50% progress.
        val exercisesToSolve = when {
            level is TwoDigitComputationLevel -> totalFacts / 2
            level.strategy == ExerciseStrategy.REVIEW -> totalFacts / 2
            else -> totalFacts + (totalFacts / 2)
        }

        // ACT: Solve exercises with GOLD speed (500ms)
        repeat(exercisesToSolve) {
            val exercise = strategy.getNextExercise() ?: return@repeat
            exercise.solve(exercise.equation.getExpectedResult(), 500)
            strategy.recordAttempt(exercise, wasCorrect = true)
        }

        // Setup ViewModel
        val mockDao = mockk<FactMasteryDao> {
            every { getAllFactsForUser(activeUserId) } returns flowOf(userMastery.values.toList())
        }
        val viewModel = ProgressReportViewModel(mockDao, activeUserId)

        // ASSERT
        // Filter to ensure we get a non-empty emission (ViewModel initial state is empty)
        val progressMap = viewModel.levelProgressByOperation.filter { it.isNotEmpty() }.first()
        val levelProgress = progressMap[level.operation]?.get(level.id)?.progress ?: 0f

        // Expectation: ~50% progress
        val expectedProgress = 0.5f
        val tolerance = 0.15f 

        assertTrue(
            "Level ${level.id}: Progress $levelProgress should be close to $expectedProgress (Solved $exercisesToSolve/$totalFacts)",
            levelProgress in (expectedProgress - tolerance)..(expectedProgress + tolerance)
        )
    }
}
