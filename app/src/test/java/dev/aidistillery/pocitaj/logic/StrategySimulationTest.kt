package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

val ENABLE_DETAILED_LOGGING = false

/**
 * A powerful simulation harness to test and compare the long-term behavior of different
 * learning strategies against various student personas.
 */
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
        // Optional hooks for the model to update its internal state after an attempt
        fun recordAttempt(context: SimulationContext, wasCorrect: Boolean) {}
    }

    private class PerfectStudent : StudentModel {
        override val name = "PERFECT"
        override fun getSuccessProbability(context: SimulationContext) = 1.0
    }

    private class GoodStudent : StudentModel {
        override val name = "GOOD"
        override fun getSuccessProbability(context: SimulationContext) = 0.9
    }

    private class ImprovingStudent : StudentModel {
        override val name = "IMPROVING"
        override fun getSuccessProbability(context: SimulationContext) = (0.5 + (context.attempts * 0.1)).coerceAtMost(1.0)
    }

    private class ForgetfulStudent : StudentModel {
        override val name = "FORGETFUL"
        override fun getSuccessProbability(context: SimulationContext): Double {
            return if (context.totalCorrectAnswers >= 10) {
                0.99
            } else if (context.lastSeenPosition == null) {
                0.5
            } else {
                val exercisesSince = context.currentPosition - context.lastSeenPosition - 1
                (1.0 - (exercisesSince * 0.03)).coerceAtLeast(0.0)
            }
        }
    }

    private open class PowerLawStudent : StudentModel {
        override val name = "POWER_LAW"
        internal val recallStrengths = mutableMapOf<String, Double>()
        internal val learningFloors = mutableMapOf<String, Double>()
        open val alphaSuccess = 0.1
        open val alphaFailure = 0.02

        override fun getSuccessProbability(context: SimulationContext): Double {
            return if (context.lastSeenPosition == null) {
                0.4 // Initial guess for a new fact
            } else {
                val timeSince = (context.currentPosition - context.lastSeenPosition).toDouble()
                // The probability of recall decays exponentially over time.
                Math.pow(context.recallStrength, timeSince)
            }
        }

        override fun recordAttempt(context: SimulationContext, wasCorrect: Boolean) {
            val floor = learningFloors.getOrDefault(context.factId, 0.3)
            val currentStrength = recallStrengths.getOrDefault(context.factId, 0.5)

            // The Power Law of Practice: learning is proportional to the gap to mastery.
            val alpha = if (wasCorrect) alphaSuccess else alphaFailure // Learning rate
            val newFloor = floor + (alpha * (1.0 - floor))
            learningFloors[context.factId] = newFloor

            val newStrength = if (wasCorrect) {
                (currentStrength + 0.1).coerceAtMost(0.95)
            } else {
                // Penalty for incorrect answer cannot go below the new, higher floor.
                (currentStrength - 0.1).coerceAtLeast(newFloor)
            }
            recallStrengths[context.factId] = newStrength
        }
    }

    private class FastPowerLawStudent : PowerLawStudent() {
        override val name = "FAST_POWER_LAW"
        override val alphaSuccess = 0.2 // Learns twice as fast from success
        override val alphaFailure = 0.04 // Learns twice as fast from failure
    }


    /**
     * A rich data class to hold the results of a simulation run.
     */
    private data class StrategyPerformanceProfile(
        val strategyName: String,
        val studentProfile: String,
        val finalStrengthDistribution: Map<Int, Int>,
        val uniqueFactsShown: Int,
        val coverage: Double,
        val repetitionRate: Double,
        val learningVelocity: Double,
        val wastedRepetitions: Int
    )

    /**
     * Runs a full simulation for a given strategy and student profile.
     */
    private fun runStrategySimulation(
        level: Level,
        iterations: Int,
        sessionLength: Int,
        studentModel: StudentModel,
        strategyProvider: (Level, MutableMap<String, FactMastery>) -> ExerciseProvider
    ): StrategyPerformanceProfile {
        val userMastery = mutableMapOf<String, FactMastery>()
        val history = mutableListOf<Pair<String, Int>>() // FactId and its strength at the time
        val attemptCounts = mutableMapOf<String, Int>()
        val correctCounts = mutableMapOf<String, Int>()
        val lastSeenPositions = mutableMapOf<String, Int>()
        var totalStrengthGains = 0
        var strategy: ExerciseProvider? = null

        repeat(iterations) { i ->
            val isNewSession = sessionLength > 0 && i % sessionLength == 0
            if (strategy == null || isNewSession) {
                strategy = strategyProvider(level, userMastery)
            }

            val exercise = strategy!!.getNextExercise()
            if (exercise == null) {
                strategy = null
                return@repeat
            }

            val factId = exercise.getFactId()
            val initialStrength = userMastery[factId]?.strength ?: 0
            history.add(factId to initialStrength)
            attemptCounts[factId] = (attemptCounts[factId] ?: 0) + 1

            val recallStrength = (studentModel as? PowerLawStudent)?.recallStrengths?.getOrDefault(factId, 0.5) ?: 0.5
            val context = SimulationContext(
                factId = factId,
                strength = initialStrength,
                totalCorrectAnswers = correctCounts.getOrDefault(factId, 0),
                attempts = attemptCounts.getOrDefault(factId, 0),
                lastSeenPosition = lastSeenPositions[factId],
                currentPosition = i,
                recallStrength = recallStrength
            )
            val wasCorrect = Random.nextDouble() < studentModel.getSuccessProbability(context)

            if (wasCorrect) {
                correctCounts[factId] = (correctCounts[factId] ?: 0) + 1
            }
            lastSeenPositions[factId] = i
            studentModel.recordAttempt(context, wasCorrect)

            if (ENABLE_DETAILED_LOGGING && strategy is DrillStrategy && studentModel is PowerLawStudent) {
                val floor = studentModel.learningFloors[factId]
                println("i=$i, fact=$factId, correct=$wasCorrect, learningFloor -> ${"%.3f".format(floor)}")
            }

            strategy!!.recordAttempt(exercise, wasCorrect)

            val finalStrength = userMastery[factId]?.strength ?: initialStrength
            if (finalStrength > initialStrength) {
                totalStrengthGains++
            }
        }

        val finalMastery = userMastery.values
        val uniqueFactsShown = finalMastery.size
        val totalFacts = level.getAllPossibleFactIds().size
        val learningVelocity = if (totalStrengthGains > 0) history.size.toDouble() / totalStrengthGains else 0.0

        return StrategyPerformanceProfile(
            strategyName = strategy!!::class.java.simpleName,
            studentProfile = studentModel.name,
            finalStrengthDistribution = finalMastery.groupingBy { it.strength }.eachCount(),
            uniqueFactsShown = uniqueFactsShown,
            coverage = uniqueFactsShown.toDouble() / totalFacts,
            repetitionRate = (history.size - uniqueFactsShown).toDouble() / history.size,
            learningVelocity = learningVelocity,
            wastedRepetitions = history.count { (_, strength) -> strength >= 5 }
        )
    }

    @Test
    fun compare_all_strategies_and_students() {
        val iterations = 100
        val strategies = mapOf(
            "ReviewStrategy" to ({ l: Level, m: MutableMap<String, FactMastery> -> ReviewStrategy(l, m) } to 0),
            "DrillStrategy" to ({ l: Level, m: MutableMap<String, FactMastery> -> DrillStrategy(l, m, 4) } to 20)
        )
        val students = listOf(PerfectStudent(), ImprovingStudent(), GoodStudent(), ForgetfulStudent(), PowerLawStudent(), FastPowerLawStudent())

        students.forEach { student ->
            println("\n--- SIMULATION FOR STUDENT: ${student.name} ---")
            println("| Strategy         | Touched | Coverage | Rep Rate | Velocity | Wasted | Final Strengths Distribution")
            println("|------------------|---------|----------|----------|----------|--------|--------------------------------")

            strategies.forEach { (_, pair) ->
                val (provider, sessionLength) = pair
                val result = runStrategySimulation(testLevel, iterations, sessionLength, student, provider)

                val distString = result.finalStrengthDistribution.entries.sortedBy { it.key }
                    .joinToString(", ") { "S${it.key}:${it.value}" }

                println(
                    "| ${result.strategyName.padEnd(16)} | " +
                            "${result.uniqueFactsShown.toString().padEnd(7)} | " +
                            "${"%.0f%%".format(result.coverage * 100).padEnd(8)} | " +
                            "${"%.0f%%".format(result.repetitionRate * 100).padEnd(8)} | " +
                            "${"%.2f".format(result.learningVelocity).padEnd(8)} | " +
                            "${result.wastedRepetitions.toString().padEnd(6)} | " +
                            distString
                )
                assertTrue("Should always touch at least one fact", result.uniqueFactsShown > 0)
            }
        }
    }
}

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
