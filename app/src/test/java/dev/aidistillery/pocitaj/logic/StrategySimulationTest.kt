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

    // --- Simulation Configuration ---
    private val RUNS_PER_LEVEL = 3

    // --- Student Persona Modeling ---

    private interface StudentModel {
        val name: String
        fun getSuccessProbability(): Double
        fun getAttemptDuration(factId: String): Long
    }

    private class PerfectStudent : StudentModel {
        override val name = "PERFECT"
        // 100% Accuracy, Always Gold Speed (500ms)
        override fun getSuccessProbability() = 1.0
        override fun getAttemptDuration(factId: String) = 500L
    }

    private class MediocreStudent : StudentModel {
        override val name = "MEDIOCRE"
        // 97% Accuracy to avoid "reset loops", Speed varies between Silver and Bronze
        override fun getSuccessProbability() = 0.97

        override fun getAttemptDuration(factId: String): Long {
            val (op, op1, op2) = getOpsFromFactId(factId)
            val threshold = getSpeedThreshold(op, op1, op2)
            // Mix of Silver (0.6) and Bronze (0.9) - 50/50 chance
            val factor = if (Random.nextBoolean()) 0.6 else 0.9
            return (threshold * factor).toLong()
        }
    }

    private class BadStudent : StudentModel {
        override val name = "BAD"
        // 95% Accuracy, Speed is mostly Bronze (65%) with some Silver (35%)
        override fun getSuccessProbability() = 0.95

        override fun getAttemptDuration(factId: String): Long {
            val (op, op1, op2) = getOpsFromFactId(factId)
            val threshold = getSpeedThreshold(op, op1, op2)
            // Constant probability: 35% chance of Silver (0.7), 65% chance of Bronze (1.05)
            val factor = if (Random.nextDouble() < 0.35) 0.7 else 1.05
            return (threshold * factor).toLong()
        }
    }

    // --- Test Execution ---

    @Test
    fun simplify_strategies_and_levels() {
        val levels = Curriculum.getAllLevels()
        val students = listOf(
            PerfectStudent(),
            MediocreStudent(),
            BadStudent()
        )

        println("\n=== SIMULATION: Median of $RUNS_PER_LEVEL Runs (Strength >= 4) ===")
        val headerFormat = "| %-25s | %-10s | %-10s | %-10s | %-10s |"
        println(headerFormat.format("Level ID", "Facts", "Perfect", "Med/Perf", "Bad/Perf"))
        println("|---------------------------|------------|------------|------------|------------|")

        levels.forEach { level ->
            val totalFacts = level.getAllPossibleFactIds().size
            if (totalFacts == 0) return@forEach

            // Data collection buckets
            val perfectScores = mutableListOf<Int>()
            val medRatios = mutableListOf<Double>()
            val badRatios = mutableListOf<Double>()

            repeat(RUNS_PER_LEVEL) {
                // Run simulation for all students in this iteration
                val results = students.map { student ->
                    runSimulationUntilMastery(level, student)
                }

                val perfect = results[0]
                val mediocre = results[1]
                val bad = results[2]

                perfectScores.add(perfect)
                if (perfect > 0) {
                    medRatios.add(mediocre.toDouble() / perfect)
                    badRatios.add(bad.toDouble() / perfect)
                }
            }

            // Calculate Stats (Median)
            val avgPerfect = perfectScores.average().toInt() // Perfect should be constant
            val medianMed = medRatios.median()
            val medianBad = badRatios.median()

            val rowFormat = "| %-25s | %-10d | %-10d | %-10s | %-10s |"
            println(
                rowFormat.format(
                    level.id,
                    totalFacts,
                    avgPerfect,
                    "%.1fx".format(medianMed),
                    "%.1fx".format(medianBad)
                )
            )
        }
    }

    // --- Simulation Logic ---

    private fun runSimulationUntilMastery(
        level: Level,
        studentModel: StudentModel,
        maxExercises: Int = 10000 // Safety break
    ): Int {
        var strategy: ExerciseProvider? = null
        val userMastery = mutableMapOf<String, FactMastery>()
        val attemptCounts = mutableMapOf<String, Int>()

        // Dummy clock
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(0L)
        }

        // Factory for Strategy
        val strategyProvider = { l: Level, m: MutableMap<String, FactMastery>, c: Clock ->
            if (l is TwoDigitAdditionLevel) {
                TwoDigitAdditionDrillStrategy(l, m, activeUserId = 1L, clock = c)
            } else {
                DrillStrategy(l, m, activeUserId = 1L, clock = c)
            }
        }

        var exercisesCount = 0
        val requiredFacts = getRequiredFactsForMastery(level)

        while (exercisesCount < maxExercises) {
            if (strategy == null) {
                strategy = strategyProvider(level, userMastery, clock)
            }

            // 1. Mastery Check
            val allMastered = requiredFacts.all { factId ->
                (userMastery[factId]?.strength ?: 0) >= 4
            }
            if (allMastered) return exercisesCount

            // 2. Get Next Exercise
            val exercise = strategy?.getNextExercise() ?: return exercisesCount 

            exercisesCount++
            val factId = exercise.getFactId()
            attemptCounts[factId] = (attemptCounts[factId] ?: 0) + 1

            // 3. Simulate Student Attempt
            val probability = studentModel.getSuccessProbability()
            val wasCorrect = Random.nextDouble() < probability
            val duration = studentModel.getAttemptDuration(factId)

            exercise.timeTakenMillis = duration.toInt()
            
            // SpeedBadge logic (needed for Strength promotion in DrillStrategy)
            val (op, op1, op2) = getOpsFromFactId(factId)
            exercise.speedBadge = getSpeedBadge(op, op1, op2, duration)

            // 4. Record Result
            strategy?.recordAttempt(exercise, wasCorrect)
        }

        return maxExercises
    }

    // --- Helpers ---

    private fun getRequiredFactsForMastery(level: Level): Set<String> {
        val allFactIds = level.getAllPossibleFactIds()
        
        if (level is TwoDigitAdditionLevel) {
            // For TwoDigitAddition, DrillStrategy manages mastery on the *underlying* single-digit facts (ones and tens),
            // not on the composite problem ID itself (e.g. ADD_ONES_3_4_...).
            // We verify mastery by checking if all underlying facts are >= 4.
            return allFactIds.flatMap { factId ->
                 val parts = factId.split("_")
                 val ones = "ADD_ONES_${parts[2]}_${parts[3]}" // e.g. ADD_ONES_3_4
                 val tens = "ADD_TENS_${parts[6]}_${parts[7]}" // e.g. ADD_TENS_1_2
                 listOf(ones, tens)
            }.toSet()
        } else {
            return allFactIds.toSet()
        }
    }

    private fun List<Double>.median(): Double {
        if (isEmpty()) return 0.0
        val sorted = sorted()
        val middle = size / 2
        return if (size % 2 == 1) {
            sorted[middle]
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        }
    }
    
    companion object {
        // Static helper to extract Operation and operands from Fact ID
        fun getOpsFromFactId(factId: String): Triple<Operation, Int, Int> {
            val parts = factId.split("_")
            if (factId.startsWith("ADD_ONES")) {
                // ADD_ONES_o1_o2_ADD_TENS_t1_t2
                val o1 = parts[2].toInt()
                val o2 = parts[3].toInt()
                val t1 = parts[6].toInt()
                val t2 = parts[7].toInt()
                return Triple(Operation.ADDITION, t1 * 10 + o1, t2 * 10 + o2)
            } else {
                // Standard: OPERATION_OP1_OP2
                return Triple(Operation.valueOf(parts[0]), parts[1].toInt(), parts[2].toInt())
            }
        }
    }
}