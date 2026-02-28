package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
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
    fun simulate_drills_on_all_levels() {
        val levels = Curriculum.getAllLevels()

        println("\n=== SIMULATION: Focused Drills (Strength >= 4) ===")
        val headerFormat = "| %-25s | %-10s | %-10s | %-13s |"
        println(headerFormat.format("Level ID", "Facts", "Perfect", "Mistaken/Perf"))
        println("|---------------------------|------------|------------|---------------|")

        levels.forEach { level ->
            val totalFacts = level.getAllPossibleFactIds().size
            if (totalFacts == 0) return@forEach

            val perfect =
                runSimulationUntilMastery(listOf(level), PerfectStudent(), isSmartPractice = false)
            val mistaken = runSimulationUntilMastery(
                listOf(level),
                MistakeProneStudent(),
                isSmartPractice = false
            )
            
            val factsCountReported = perfect.factsCount
            val uniqueQueriesReported = perfect.uniqueQueries
            
            val ratio = if (perfect.exerciseCount > 0) {
                mistaken.exerciseCount.toDouble() / perfect.exerciseCount
            } else 0.0

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

    @Test
    fun simulate_smart_practice_on_addition() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val allFacts = levels.flatMap { it.getAllPossibleFactIds() }.distinct()

        println("\n=== SIMULATION: Smart Practice (Addition) ===")
        println("Total Facts in Operation: ${allFacts.size}")

        val perfect = runSimulationUntilMastery(levels, PerfectStudent(), isSmartPractice = true)
        val mistaken =
            runSimulationUntilMastery(levels, MistakeProneStudent(), isSmartPractice = true)

        println(
            "Perfect Student Exercises: ${perfect.exerciseCount} (Efficiency: %.2f ex/fact)".format(
                perfect.exerciseCount.toDouble() / allFacts.size
            )
        )
        println(
            "Mistaken Student Exercises: ${mistaken.exerciseCount} (Efficiency: %.2f ex/fact)".format(
                mistaken.exerciseCount.toDouble() / allFacts.size
            )
        )

        val ratio = mistaken.exerciseCount.toDouble() / perfect.exerciseCount
        println("Penalty Ratio: %.1fx".format(ratio))
    }

    // --- Simulation Logic ---

    private fun runSimulationUntilMastery(
        curriculum: List<Level>,
        studentModel: StudentModel,
        isSmartPractice: Boolean,
        maxExercises: Int = 20000 // Safety break (raised for full operation)
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
        val strategyProvider =
            { levels: List<Level>, m: MutableMap<String, FactMastery>, c: Clock ->
                if (isSmartPractice) {
                    SmartPracticeStrategy(levels, m, activeUserId = 1L, random = random, clock = c)
                } else {
                    levels.first().createStrategy(m, activeUserId = 1L, clock = c, random = random)
                }
        }

        var exercisesCount = 0
        val requiredFacts = curriculum.flatMap { it.getAllPossibleFactIds() }.toSet()

        while (exercisesCount < maxExercises) {
            if (strategy == null) {
                strategy = strategyProvider(curriculum, userMastery, clock)
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

            // Missing Operand: a + ? = b or ? + b = c
            val missingAddMatchMiddle = Regex("""(\d+) \+ \? = (\d+)""").matchEntire(factId)
            if (missingAddMatchMiddle != null) {
                val (a, result) = missingAddMatchMiddle.destructured
                val res = result.toInt()
                val op1 = a.toInt()
                val op2 = res - op1
                return Triple(Operation.ADDITION, op1, op2) 
            }
            val missingAddMatchStart = Regex("""\? \+ (\d+) = (\d+)""").matchEntire(factId)
            if (missingAddMatchStart != null) {
                val (b, result) = missingAddMatchStart.destructured
                val res = result.toInt()
                val op2 = b.toInt()
                val op1 = res - op2
                return Triple(Operation.ADDITION, op1, op2)
            }

            val missingSubMatchMiddle = Regex("""(\d+) - \? = (\d+)""").matchEntire(factId)
            if (missingSubMatchMiddle != null) {
                val (a, result) = missingSubMatchMiddle.destructured
                val res = result.toInt()
                val op1 = a.toInt()
                val op2 = op1 - res // a - b = res -> a - res = b
                return Triple(Operation.SUBTRACTION, op1, op2)
            }
            val missingSubMatchStart = Regex("""\? - (\d+) = (\d+)""").matchEntire(factId)
            if (missingSubMatchStart != null) {
                val (b, result) = missingSubMatchStart.destructured
                val res = result.toInt()
                val op2 = b.toInt()
                val op1 = res + op2 // a - b = res -> a = res + b
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