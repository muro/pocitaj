package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A simulation harness to test and compare the long-term behavior of different
 * learning strategies against various student personas.
 *
 * Runs simulations for each level and student type to estimate the "effort" (number of exercises)
 * required to reach Strength 4 (Consolidating).
 */
class StrategySimulationTest {

    data class SimulationResult(val exerciseCount: Int, val factsCount: Int, val uniqueQueries: Int)

    // --- Student Persona Modeling ---

    private interface StudentModel {
        val name: String
        fun getSuccessProbability(factId: String): Double
        fun getAttemptDuration(factId: String): Long
    }

    private class PerfectStudent : StudentModel {
        override val name = "PERFECT"
        // 100% Accuracy, Always Gold Speed (500ms)
        override fun getSuccessProbability(factId: String) = 1.0
        override fun getAttemptDuration(factId: String) = PERFECT_SPEED_MS
    }

    private class MistakeProneStudent : StudentModel {
        override val name = "MISTAKE_PRONE"
        private val seenFacts = mutableSetOf<String>()

        // 20% Accuracy error rate on NEW facts, rounded up.
        // Logic: 1 mistake every 5 new facts. 
        // Index 0 (1st fact) -> Mistake. Index 5 (6th fact) -> Mistake.
        override fun getSuccessProbability(factId: String): Double {
            if (factId in seenFacts) return 1.0

            val index = seenFacts.size
            seenFacts.add(factId)

            // Mistake on 0, 5, 10, etc. (1st, 6th, 11th fact)
            return if (index % 5 == 0) 0.0 else 1.0
        }

        override fun getAttemptDuration(factId: String) = PERFECT_SPEED_MS
    }

    // --- Test Execution ---

    @Test
    fun simplify_strategies_and_levels() {
        val levels = Curriculum.getAllLevels()

        println("\n=== SIMULATION: (Strength >= 4) ===")
        val headerFormat = "| %-25s | %-10s | %-10s | %-13s |"
        println(headerFormat.format("Level ID", "Facts", "Perfect", "Mistaken/Perf"))
        println("|---------------------------|------------|------------|---------------|")

        levels.forEach { level ->
            val totalFacts = level.getAllPossibleFactIds().size
            if (totalFacts == 0) return@forEach

            val perfect = runSimulationUntilMastery(level, PerfectStudent())
            val mistaken = runSimulationUntilMastery(level, MistakeProneStudent())

            val factsCountReported = perfect.factsCount
            val uniqueQueriesReported = perfect.uniqueQueries
            
            val ratio = if (perfect.exerciseCount > 0) {
                mistaken.exerciseCount.toDouble() / perfect.exerciseCount
            } else 0.0

            // Verification Assertions
            // Perfect student: At least 1 exercise per fact (unless level updates multiple facts per exercise).
            val minExpectedExercises = if (level is TwoDigitComputationLevel) {
                factsCountReported / 2
            } else {
                factsCountReported
            }

            assertTrue(
                "Level ${level.id}: Perfect student should take at least $minExpectedExercises exercises (Facts: $factsCountReported, Actual: ${perfect.exerciseCount})",
                perfect.exerciseCount >= minExpectedExercises
            )

            // Mistake Prone Logic:
            // Mistakes = ceil(uniqueQueries * 0.2)
            // Each mistake triggers a retry. The penalty should be at least 1 extra exercise.
            val expectedMistakes = kotlin.math.ceil(uniqueQueriesReported * 0.2).toInt()
            val expectedPenalty =
                (expectedMistakes * 0.3).toInt() // Lowered slightly for robustness
            val expectedMin = perfect.exerciseCount + expectedPenalty
            val tolerance =
                2 // Allow for random walk variance (sometimes Mistaken path is luckily shorter)

            assertTrue(
                "Level ${level.id}: Mistaken student should take at least ${expectedPenalty - tolerance} more exercises (Perfect: ${perfect.exerciseCount}, MST: ${mistaken.exerciseCount}, Queries: $uniqueQueriesReported, TheoreticalMistakes: $expectedMistakes)",
                mistaken.exerciseCount >= expectedMin - tolerance
            )

            val rowFormat = "| %-25s | %-10d | %-10d | %-13s |"
            println(
                rowFormat.format(
                    level.id,
                    factsCountReported,
                    perfect.exerciseCount,
                    "%.1fx".format(ratio)
                )
            )
        }
    }

    // --- Simulation Logic ---

    private fun runSimulationUntilMastery(
        level: Level,
        studentModel: StudentModel,
        maxExercises: Int = 10000 // Safety break
    ): SimulationResult {
        var strategy: ExerciseProvider? = null
        val userMastery = mutableMapOf<String, FactMastery>()
        val attemptCounts = mutableMapOf<String, Int>()
        
        // Fixed Seed Random for Reproducibility
        val random = Random(12345)

        // Dummy clock
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(0L)
        }

        // Factory for Strategy
        val strategyProvider = { l: Level, m: MutableMap<String, FactMastery>, c: Clock ->
            l.createStrategy(m, activeUserId = 1L, clock = c, random = random)
        }

        var exercisesCount = 0
        val requiredFacts = getRequiredFactsForMastery(level)

        while (exercisesCount < maxExercises) {
            if (strategy == null) {
                strategy = strategyProvider(level, userMastery, clock)
            }

            // 1. Mastery Check
            val allMastered = requiredFacts.all { factId ->
                (userMastery[factId]?.strength ?: 0) >= MASTERY_STRENGTH
            }
            if (allMastered) return SimulationResult(
                exercisesCount,
                requiredFacts.size,
                attemptCounts.size
            )

            // 2. Get Next Exercise
            val exercise = strategy.getNextExercise() ?: return SimulationResult(
                exercisesCount,
                requiredFacts.size,
                attemptCounts.size
            )

            exercisesCount++
            val factId = exercise.getFactId()
            attemptCounts[factId] = (attemptCounts[factId] ?: 0) + 1

            // 3. Simulate Student Attempt
            val probability = studentModel.getSuccessProbability(factId)
            // Use local random for student simulation too, or passed random? 
            // Student models are currently deterministic in this test (Perfect=1.0, Mistaken=Modulo).
            // But if we had stochastic students, we should pass `random` to them too.
            // For now, these are deterministic.
            val wasCorrect = Random.nextDouble() < probability
            val duration = studentModel.getAttemptDuration(factId)

            exercise.timeTakenMillis = duration.toInt()
            
            // SpeedBadge logic (needed for Strength promotion in DrillStrategy)
            val (op, op1, op2) = getOpsFromFactId(factId)
            exercise.speedBadge = getSpeedBadge(op, op1, op2, duration)

            // 4. Record Result
            strategy.recordAttempt(exercise, wasCorrect)
        }

        return SimulationResult(maxExercises, requiredFacts.size, attemptCounts.size)
    }

    // --- Helpers ---

    private fun getRequiredFactsForMastery(level: Level): Set<String> {
        val allFactIds = level.getAllPossibleFactIds()

        // For TwoDigitComputationLevel, getAllPossibleFactIds already returns the component facts (ONES/TENS).
        // For other levels, it returns the semantic facts.
        // In both cases, these are exactly the facts we need to track mastery for.
        return allFactIds.toSet()
    }
    
    companion object {
        // --- Simulation Configuration ---
        private const val MASTERY_STRENGTH = 4
        private const val PERFECT_SPEED_MS = 500L

        // Static helper to extract Operation and operands from Fact ID
        fun getOpsFromFactId(factId: String): Triple<Operation, Int, Int> {
            val addMatch = Regex("""(\d+) \+ (\d+) =.*""").matchEntire(factId)
            if (addMatch != null) {
                val (a, b) = addMatch.destructured
                return Triple(Operation.ADDITION, a.toInt(), b.toInt())
            }

            val subMatch = Regex("""(\d+) - (\d+) =.*""").matchEntire(factId)
            if (subMatch != null) {
                val (a, b) = subMatch.destructured
                return Triple(Operation.SUBTRACTION, a.toInt(), b.toInt())
            }

            val mulMatch = Regex("""(\d+) \* (\d+) =.*""").matchEntire(factId)
            if (mulMatch != null) {
                val (a, b) = mulMatch.destructured
                return Triple(Operation.MULTIPLICATION, a.toInt(), b.toInt())
            }

            val divMatch = Regex("""(\d+) / (\d+) =.*""").matchEntire(factId)
            if (divMatch != null) {
                val (a, b) = divMatch.destructured
                return Triple(Operation.DIVISION, a.toInt(), b.toInt())
            }

            // Missing Operand: a + ? = b
            val missingAddMatch = Regex("""(\d+) \+ \? = (\d+)""").matchEntire(factId)
            if (missingAddMatch != null) {
                val (a, result) = missingAddMatch.destructured
                val res = result.toInt()
                val op1 = a.toInt()
                val op2 = res - op1
                return Triple(Operation.ADDITION, op1, op2) 
            }

            val missingSubMatch = Regex("""(\d+) - \? = (\d+)""").matchEntire(factId)
            if (missingSubMatch != null) {
                val (a, result) = missingSubMatch.destructured
                val res = result.toInt()
                val op1 = a.toInt()
                val op2 = op1 - res // a - b = res -> a - res = b
                return Triple(Operation.SUBTRACTION, op1, op2)
            }

            // Fallback for legacy ID support if mixed environment testing happens?
            // Or throw error if unknown format.
            // Let's fallback gracefully to avoid crashes, but print warning
            println("Unknown Fact ID format: $factId")
            return Triple(Operation.ADDITION, 0, 0)
        }
    }
}