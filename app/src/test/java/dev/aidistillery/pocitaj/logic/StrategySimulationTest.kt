package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
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
        fun getSuccessProbability(factId: String): Double
        fun getAttemptDuration(factId: String): Long
    }

    class PerfectStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 1.0
        override fun getAttemptDuration(factId: String) = PERFECT_SPEED_MS
    }

    class MistakeProneStudent : StudentModel {
        private val seenFacts = mutableSetOf<String>()

        override fun getSuccessProbability(factId: String): Double {
            if (factId in seenFacts) return 1.0

            val index = seenFacts.size
            seenFacts.add(factId)

            // Mistake on 0, 5, 10, etc. (1st, 6th, 11th fact)
            return if (index % 5 == 0) 0.0 else 1.0
        }

        override fun getAttemptDuration(factId: String) = PERFECT_SPEED_MS
    }

    /**
     * Persona 1: New - Advanced but Struggling
     * Cold start (no history), Gold speed on low numbers, struggling on multi-digit.
     */
    class AdvancedStrugglingStudent : StudentModel {
        override fun getSuccessProbability(factId: String): Double {
            val (_, op1, op2) = getOpsFromFactId(factId)
            return if (op1 <= 10 && op2 <= 10) 1.0 else 0.70
        }

        override fun getAttemptDuration(factId: String): Long {
            val (_, op1, op2) = getOpsFromFactId(factId)
            return if (op1 <= 10 && op2 <= 10) 500L else 4000L
        }
    }

    /**
     * Persona 2: New - Pure Beginner
     * No history, slow and steady, common mistakes.
     */
    class PureBeginnerStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 0.85
        override fun getAttemptDuration(factId: String) = 3000L
    }

    /**
     * Persona 3: New - Grand Master
     * Cold start, knows everything perfectly.
     */
    class GrandMasterStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 1.0
        override fun getAttemptDuration(factId: String) = 400L
    }

    /**
     * Persona 4: Returning - Rusty Veteran
     * History exist (Strength 5), but behavior is now slow/rusty.
     */
    class RustyVeteranStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 0.9
        override fun getAttemptDuration(factId: String) = 2500L
    }

    /**
     * Persona 5: Returning - Active Mid-Way
     * History exists, behavior is consistent.
     */
    class MidWayStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 0.95
        override fun getAttemptDuration(factId: String) = 1500L
    }

    // --- Test Execution ---

    @Test
    fun simulate_drill_strategies_per_level() {
        val drillLevels = Curriculum.getAllLevels().filter { it.strategy == ExerciseStrategy.DRILL }
        runStrategySimulationTable("Focused Drills", drillLevels)
    }

    @Test
    fun simulate_review_strategies_per_level() {
        val reviewLevels = Curriculum.getAllLevels().filter { it.strategy == ExerciseStrategy.REVIEW }
        runStrategySimulationTable("Focused Reviews", reviewLevels)
    }

    private fun runStrategySimulationTable(title: String, levels: List<Level>) {
        println("\n=== SIMULATION: $title (Strength >= 4) ===")
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

            // Verification Assertions (Restored for stability)
            val minExpectedExercises = if (level is TwoDigitComputationLevel) {
                factsCountReported / 2
            } else {
                factsCountReported
            }

            withClue("Level ${level.id}: Perfect student should take at least $minExpectedExercises exercises (Facts: $factsCountReported, Actual: ${perfect.exerciseCount})") {
                (perfect.exerciseCount >= minExpectedExercises) shouldBe true
            }

            val expectedMistakes = kotlin.math.ceil(uniqueQueriesReported * 0.2).toInt()
            val expectedPenalty = (expectedMistakes * 0.3).toInt() 
            val expectedMin = perfect.exerciseCount + expectedPenalty
            val tolerance = 2 

            withClue("Level ${level.id}: Mistaken student should take at least ${expectedPenalty - tolerance} more exercises (Perfect: ${perfect.exerciseCount}, MST: ${mistaken.exerciseCount}, Queries: $uniqueQueriesReported, TheoreticalMistakes: $expectedMistakes)") {
                (mistaken.exerciseCount >= expectedMin - tolerance) shouldBe true
            }

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

    @Test
    fun simulate_adaptability_pure_beginner() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        
        println("\n=== ADAPTABILITY REPORT: Pure Beginner (Cold Start) ===")
        // Using 2400ms to allow them to barely pass Bronze thresholds (2500ms)
        // and progress, otherwise they get stuck at Strength 2.
        val beginner = object : StudentModel by PureBeginnerStudent() {
            override fun getAttemptDuration(factId: String) = 2400L 
        }

        val result = runSimulationUntilMastery(
            levels,
            beginner,
            isSmartPractice = true,
            maxExercises = 1000 // Limit for readability
        )
        
        println("Exercises Run: ${result.exerciseCount}")
        println("Unique Facts Encountered: ${result.uniqueQueries}")
        
        if (result.exerciseCount >= 1000) {
            println("Status: STRUGGLING (Did not complete operation within 1000 exercises)")
        } else {
            println("Status: COMPLETED")
        }
    }

    // Placeholder tests for other personas (not active yet)
    /*
    @Test
    fun simulate_adaptability_advanced_struggling() { ... }
    
    @Test
    fun simulate_adaptability_grand_master() { ... }
    
    @Test
    fun simulate_adaptability_rusty_veteran() { ... }
    
    @Test
    fun simulate_adaptability_mid_way() { ... }
    */

    // --- Simulation Logic ---

    private fun runSimulationUntilMastery(
        curriculum: List<Level>,
        studentModel: StudentModel,
        isSmartPractice: Boolean,
        initialMastery: Map<String, FactMastery> = emptyMap(),
        maxExercises: Int = 20000 // Safety break (raised for full operation)
    ): SimulationResult {
        var strategy: ExerciseProvider? = null
        val userMastery = initialMastery.toMutableMap()
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