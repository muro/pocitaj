package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

// trick to avoid warning about condition always false / true when used.
private val ENABLE_DETAILED_LOGGING = "false".toBoolean()

/**
 * A powerful simulation harness to test and compare the long-term behavior of different
 * learning strategies against various student personas.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StrategySimulationTest {

    // --- Test Configuration ---

    private val testLevel = object : Level {
        override val id = "TEST_LEVEL_SIM"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.REVIEW
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = (1..20).map { "ADDITION_1_${it}" }
    }

    // --- Student Persona Modeling (Strategy Pattern) ---

    /**
     * A context object holding all the information a student model might need to decide
     * the probability of answering a question correctly.
     */
    private data class SimulationContext(
        val factId: String,
        val strength: Int,
        val totalCorrectAnswers: Int,
        val attempts: Int,
        val lastSeenPosition: Int?,
        val currentPosition: Int,
        val recallStrength: Double
    )

    /**
     * Defines the interface for a student model. Each implementation represents a
     * different learning or forgetting behavior.
     */
    private interface StudentModel {
        val name: String
        fun getSuccessProbability(context: SimulationContext): Double
        fun getAttemptDuration(context: SimulationContext): Long

        // Optional hooks for the model to update its internal state after an attempt
        fun recordAttempt(context: SimulationContext, wasCorrect: Boolean) {}
    }

    private class PerfectStudent : StudentModel {
        override val name = "PERFECT"

        // Key: 100% Accuracy, Always Gold Speed
        override fun getSuccessProbability(context: SimulationContext) = 1.0
        override fun getAttemptDuration(context: SimulationContext): Long = 500L // Always Gold
    }

    private class MediocreStudent : StudentModel {
        override val name = "MEDIOCRE"

        // Key: 96% Accuracy (occasional mistake), Speed varies between Silver and Bronze
        override fun getSuccessProbability(context: SimulationContext) = 0.96

        override fun getAttemptDuration(context: SimulationContext): Long {
            val exercise = exerciseFromFactId(context.factId)
            val (op, op1, op2) = exercise.equation.getFact()
            val threshold = getSpeedThreshold(op, op1, op2)
            // Mix of Silver (0.6) and Bronze (0.9)
            val factor = if (Random.nextBoolean()) 0.6 else 0.9
            return (threshold * factor).toLong() 
        }
    }

    private class BadStudent : StudentModel {
        override val name = "BAD"

        // Key: 94% Accuracy, Constant 20% chance of Silver speed, otherwise Bronze
        override fun getSuccessProbability(context: SimulationContext) = 0.94

        override fun getAttemptDuration(context: SimulationContext): Long {
            val exercise = exerciseFromFactId(context.factId)
            val (op, op1, op2) = exercise.equation.getFact()
            val threshold = getSpeedThreshold(op, op1, op2)

            // Constant probability: 35% chance of Silver (0.7), 65% chance of Bronze (1.05)
            // Tuned to achieve ~3x exercises compared to Perfect.
            val factor = if (Random.nextDouble() < 0.35) 0.7 else 1.05
            return (threshold * factor).toLong()
        }
    }


    /**
     * A rich data class to hold the results of a simulation run.
     */
    // ...

    @Test
    fun simplify_strategies_and_levels() {
        val levels = Curriculum.getAllLevels()
        val students = listOf(
            PerfectStudent(),
            MediocreStudent(),
            BadStudent()
        )

        println("\n=== SIMULATION: Exercises to Reach Decent Mastery (Strength >= 4) ===")
        // Formatting the header
        val headerFormat = "| %-25s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s |"
        println(
            headerFormat.format(
                "Level ID",
                "Facts",
                "Perfect",
                "Mediocre",
                "Bad",
                "Med/Perf",
                "Bad/Perf"
            )
        )
        println("|---------------------------|------------|------------|------------|------------|------------|------------|")

        levels.forEach { level ->
            val totalFacts = level.getAllPossibleFactIds().size
            if (totalFacts == 0) return@forEach // Skip empty or invalid levels

            val results = students.map { student ->
                runSimulationUntilMastery(level, student)
            }

            val row = level.id
            val perfect = results[0]
            val mediocre = results[1]
            val bad = results[2]

            val medPerfRatio =
                if (perfect > 0) "%.1fx".format(mediocre.toDouble() / perfect) else "-"
            val badPerfRatio = if (perfect > 0) "%.1fx".format(bad.toDouble() / perfect) else "-"

            val rowFormat = "| %-25s | %-10d | %-10d | %-10d | %-10d | %-10s | %-10s |"
            println(
                rowFormat.format(
                    row,
                    totalFacts,
                    perfect,
                    mediocre,
                    bad,
                    medPerfRatio,
                    badPerfRatio
                )
            )
        }
    }

    private fun runSimulationUntilMastery(
        level: Level,
        studentModel: StudentModel,
        maxExercises: Int = 10000 // Safety break
    ): Int {
        var strategy: ExerciseProvider? = null
        val userMastery = mutableMapOf<String, FactMastery>()
        val attemptCounts = mutableMapOf<String, Int>()

        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(0L) // Dummy clock
        }

        val strategyProvider = { l: Level, m: MutableMap<String, FactMastery>, c: Clock ->
            if (l is TwoDigitAdditionLevel) {
                TwoDigitAdditionDrillStrategy(l, m, activeUserId = 1L, clock = c)
            } else {
                DrillStrategy(l, m, activeUserId = 1L, clock = c)
            }
        }

        var exercisesCount = 0
        val allFactIds = level.getAllPossibleFactIds()

        // Identify all unique underlying facts we need to master
        val underlyingFactsToCheck = if (level is TwoDigitAdditionLevel) {
            val facts = allFactIds.flatMap { factId ->
                val parts = factId.split("_")
                // ADD_ONES_0_1_ADD_TENS_1_2
                // 0: ADD, 1: ONES, 2: 0, 3: 1, 4: ADD, 5: TENS, 6: 1, 7: 2
                val ones = "ADD_ONES_${parts[2]}_${parts[3]}"
                val tens = "ADD_TENS_${parts[6]}_${parts[7]}"
                listOf(ones, tens)
            }.toSet()

            // Pre-populate mastery to 2 for underlying facts (User finding: "normally starts at 2")
            facts.forEach { fid ->
                userMastery[fid] = FactMastery(fid, 1L, "", 2, 0)
            }
            facts
        } else {
            // Pre-populate mastery to 2 for all facts
            allFactIds.forEach { fid ->
                userMastery[fid] = FactMastery(fid, 1L, "", 2, 0)
            }
            allFactIds.toSet()
        }

        while (exercisesCount < maxExercises) {
            if (strategy == null) {
                strategy = strategyProvider(level, userMastery, clock)
            }

            // Check mastery condition: All required facts >= 4
            val allMastered = underlyingFactsToCheck.all { factId ->
                (userMastery[factId]?.strength ?: 0) >= 4
            }

            if (allMastered) {
                return exercisesCount
            }

            val exercise = strategy?.getNextExercise()
            if (exercise == null) {
                return exercisesCount
            }

            exercisesCount++
            val factId = exercise.getFactId()
            val attempts = attemptCounts.getOrDefault(factId, 0)
            attemptCounts[factId] = attempts + 1

            // Student attempts
            val context = SimulationContext(
                factId = factId,
                strength = 0, // Not used by simpler students
                totalCorrectAnswers = 0, // Not used
                attempts = attempts,
                lastSeenPosition = null, // Not used
                currentPosition = exercisesCount,
                recallStrength = 0.5
            )

            val probability = studentModel.getSuccessProbability(context)
            val wasCorrect = Random.nextDouble() < probability
            val duration = studentModel.getAttemptDuration(context)

            exercise.timeTakenMillis = duration.toInt()

            // SpeedBadge logic needed for promotion in DrillStrategy
            val (op, op1, op2) = getOpsFromFactId(factId)
            exercise.speedBadge = getSpeedBadge(op, op1, op2, duration)

            strategy?.recordAttempt(exercise, wasCorrect)
        }

        return maxExercises
    }

    // Helper to extract Operation and operands from Fact ID, supporting TwoDigit special IDs
    private fun getOpsFromFactId(factId: String): Triple<Operation, Int, Int> {
        if (factId.startsWith("ADD_ONES")) {
            // ADD_ONES_o1_o2_ADD_TENS_t1_t2
            val parts = factId.split("_")
            val o1 = parts[2].toInt()
            val o2 = parts[3].toInt()
            val t1 = parts[6].toInt()
            val t2 = parts[7].toInt()

            val op1 = t1 * 10 + o1
            val op2 = t2 * 10 + o2
            return Triple(Operation.ADDITION, op1, op2)
        } else {
            val parts = factId.split("_")
            // Standard: OPERATION_OP1_OP2
            val opName = parts[0]
            val op1 = parts[1].toInt()
            val op2 = parts[2].toInt()
            // Map opName "MUL" -> MULTIPLICATION if needed, but enum valueOf usually works
            // Check Curriculum definitions.
            // Most use `operation.name` which is e.g. "ADDITION".
            // But some legacy might use prefixes?
            // Curriculum.kt uses `operation.name` (full name).
            return Triple(Operation.valueOf(opName), op1, op2)
        }
    }

    private fun exerciseFromFactId(factId: String): Exercise {
        // Reuse getOpsFromFactId to construct generic exercise for duration calc
        val (op, op1, op2) = getOpsFromFactId(factId)
        return when (op) {
            Operation.ADDITION -> Exercise(Addition(op1, op2))
            Operation.SUBTRACTION -> Exercise(Subtraction(op1, op2))
            Operation.MULTIPLICATION -> Exercise(Multiplication(op1, op2))
            Operation.DIVISION -> Exercise(Division(op1, op2))
        }
    }


}

// TODO: possibly delete
@Suppress("unused")
private fun exerciseFromFactId(factId: String): Exercise {
    val parts = factId.split("_")
    val operation = Operation.valueOf(parts[0])
    val first = parts[1].toInt()
    val second = parts[2].toInt()
    return when (operation) {
        Operation.ADDITION -> Exercise(Addition(first, second))
        Operation.SUBTRACTION -> Exercise(Subtraction(first, second))
        Operation.MULTIPLICATION -> Exercise(Multiplication(first, second))
        Operation.DIVISION -> Exercise(Division(first, second))
    }
}