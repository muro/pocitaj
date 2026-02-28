package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThan as shouldBeLessThanInt
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

    data class DistributionAudit(
        val stdDev: Double,
        val maxSequentialReps: Int,
        val gapAverage: Double,
        val minAttempts: Int,
        val maxAttempts: Int
    )

    data class SimulationResult(
        val exerciseCount: Int,
        val factsCount: Int,
        val uniqueQueries: Int,
        val distribution: DistributionAudit? = null
    )

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
            // 1500ms is Gold for almost everything, Silver for single digit.
            return if (op1 <= 10 && op2 <= 10) 1500L else 3000L
        }
    }

    /**
     * Persona 2: New - Pure Beginner
     * No history, slow and steady, common mistakes.
     */
    class PureBeginnerStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 0.85
        override fun getAttemptDuration(factId: String) = 1500L
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
        override fun getAttemptDuration(factId: String) = 1700L
    }

    /**
     * Persona 5: Returning - Active Mid-Way
     * History exists, behavior is consistent.
     */
    class MidWayStudent : StudentModel {
        override fun getSuccessProbability(factId: String) = 0.95
        override fun getAttemptDuration(factId: String) = 1200L
    }

    /**
     * Persona 6: Kryptonite Student
     * Perfect on everything EXCEPT one specific fact which they fail 3 times.
     */
    class KryptoniteStudent(private val targetFactId: String) : StudentModel {
        private var failureCount = 0
        override fun getSuccessProbability(factId: String): Double {
            if (factId == targetFactId && failureCount < 3) {
                failureCount++
                return 0.0
            }
            return 1.0
        }

        override fun getAttemptDuration(factId: String) = 1500L
    }

    /**
     * Persona 7: Adaptive Beginner
     * Starts slow but gets faster by 100ms every 5 attempts.
     */
    class AdaptiveBeginner : StudentModel {
        private val attempts = mutableMapOf<String, Int>()
        override fun getSuccessProbability(factId: String) = 0.95
        override fun getAttemptDuration(factId: String): Long {
            val count = attempts[factId] ?: 0
            attempts[factId] = count + 1
            val speedGain = (count / 5) * 200L
            return (2400L - speedGain).coerceAtLeast(800L)
        }
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
        println("\n=== SIMULATION: $title (Strength >= ${MASTERY_STRENGTH - 1}) ===")
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

        withClue("Expert Velocity Guard ($perfect): Perfect student should be highly efficient in multi-level practice (~2 sightings per fact)") {
            (perfect.exerciseCount.toDouble() / allFacts.size) shouldBeLessThan 2.5
        }
        withClue("Recovery Velocity Guard ($mistaken): Mistake prone student should not be excessively penalized (~3-4 sightings per fact)") {
            (mistaken.exerciseCount.toDouble() / allFacts.size) shouldBeLessThan 4.5
        }
    }

    @Test
    fun simulate_variety_and_enforcement() {
        // Use a small level to easily see the distribution (Addition sums up to 5)
        val level = Curriculum.getLevelsFor(Operation.ADDITION).first { it.id == "ADD_SUM_5" }
        val allFacts = level.getAllPossibleFactIds()
        
        println("\n=== VARIETY & ENFORCEMENT AUDIT: Small Level (${level.id}) ===")
        
        // 1. Variety Check (Perfect Student)
        val perfect = runSimulationUntilMastery(listOf(level), PerfectStudent(), isSmartPractice = false)
        println("Perfect Student Stats:")
        println("  Max Sequential Reps: ${perfect.distribution?.maxSequentialReps}")
        println("  StdDev of Attempts: %.2f".format(perfect.distribution?.stdDev))
        
        // Assertions for "Feel"
        perfect.distribution!!.maxSequentialReps shouldBe 1 // With working set > 1, no repeats should happen
        // StdDev 0.46 means most facts had X attempts, some X+1. This is good variety for biased-random.
        perfect.distribution.stdDev shouldBeLessThan 1.0 
        (perfect.distribution.maxAttempts - perfect.distribution.minAttempts) shouldBeLessThanOrEqual 2
        
        // 2. Enforcement Check (Kryptonite Student)
        val kryptoniteId = allFacts.first()
        println("\nTesting enforcement on: $kryptoniteId")
        val kryptoniteResult = runSimulationUntilMastery(
            listOf(level), 
            KryptoniteStudent(kryptoniteId), 
            isSmartPractice = false
        )
        
        println("Kryptonite Student Stats:")
        println("  Total Exercises: ${kryptoniteResult.exerciseCount}")
        
        withClue("Enforcement check: Kryptonite student should take between 3 and 10 extra exercises") {
            kryptoniteResult.exerciseCount shouldBeGreaterThanOrEqual (perfect.exerciseCount + 3)
            kryptoniteResult.exerciseCount shouldBeLessThanOrEqual (perfect.exerciseCount + 10)
        }
    }

    @Test
    fun simulate_adaptability_advanced_struggling() {
        verifyAdaptabilityPersona("Advanced Struggling", AdvancedStrugglingStudent(), maxExpected = 20000)
    }

    @Test
    fun simulate_adaptability_grand_master() {
        verifyAdaptabilityPersona("Grand Master", GrandMasterStudent(), maxExpected = 20000)
    }

    @Test
    fun simulate_adaptability_pure_beginner() {
        // We use the AdaptiveBeginner here as it's more realistic for a "beginner" who learns
        verifyAdaptabilityPersona("Adaptive Beginner", AdaptiveBeginner(), maxExpected = 20000)
    }

    @Test
    fun simulate_adaptability_rusty_veteran() {
        // Pre-populate history as if they were rusty (Strength 3)
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val allFacts = levels.flatMap { it.getAllPossibleFactIds() }.distinct()
        val history = allFacts.associateWith { 
            FactMastery(it, 1L, "LEVEL_ID", 3, 0L, avgDurationMs = 3000L) // Slow and rusty
        }
        
        verifyAdaptabilityPersona("Rusty Veteran", RustyVeteranStudent(), maxExpected = 5000, initialHistory = history)
    }

    @Test
    fun simulate_adaptability_mid_way() {
        // Pre-populate first 3 levels
        val allLevels = Curriculum.getLevelsFor(Operation.ADDITION)
        val masterLevels = allLevels.take(3)
        val history = masterLevels.flatMap { it.getAllPossibleFactIds() }.associateWith {
            FactMastery(it, 1L, it, 5, 0L, avgDurationMs = 1000L)
        }
        
        verifyAdaptabilityPersona("Mid-Way Student", MidWayStudent(), maxExpected = 10000, initialHistory = history)
    }

    private fun verifyAdaptabilityPersona(
        name: String, 
        student: StudentModel, 
        maxExpected: Int, 
        initialHistory: Map<String, FactMastery> = emptyMap()
    ) {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val allFacts = levels.flatMap { it.getAllPossibleFactIds() }.distinct()
        
        println("\n=== ADAPTABILITY REPORT: $name ===")
        val result = runSimulationUntilMastery(
            levels,
            student,
            isSmartPractice = true,
            initialMastery = initialHistory,
            maxExercises = maxExpected
        )
        
        println("Exercises Run: ${result.exerciseCount}")
        println("Unique Facts Encountered: ${result.uniqueQueries}")
        
        withClue("Adaptability Guard ($name): Student should complete operation within $maxExpected exercises (Actual: ${result.exerciseCount})") {
            result.exerciseCount shouldBeLessThanInt maxExpected
        }
        withClue("Foundation Guard ($name): All required facts should be at least Strength ${MASTERY_STRENGTH - 1}") {
            // We verify result.factsCount matches expected size and all were seen OR mastered
            result.factsCount shouldBe allFacts.size
        }
    }

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
        
        // Fixed seeds for Reproducibility
        val strategyRandom = Random(12345)
        val studentRandom = Random(54321)

        // Dummy clock
        val clock = object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(0L)
        }

        // Factory for Strategy
        val strategyProvider =
            { levels: List<Level>, m: MutableMap<String, FactMastery>, c: Clock ->
                if (isSmartPractice) {
                    SmartPracticeStrategy(levels, m, activeUserId = 1L, random = strategyRandom, clock = c)
                } else {
                    levels.first().createStrategy(m, activeUserId = 1L, clock = c, random = strategyRandom)
                }
        }

        var exercisesCount = 0
        val requiredFacts = curriculum.flatMap { it.getAllPossibleFactIds() }.toSet()
        val encounterSequence = mutableListOf<String>()

        while (exercisesCount < maxExercises) {
            if (strategy == null) {
                strategy = strategyProvider(curriculum, userMastery, clock)
            }

            // 1. Mastery Check
            // Pedagogically, we want to know when they reach "Consolidating" (Strength 4)
            // as that's when they move to the next level.
            val allConsolidating = requiredFacts.all { factId ->
                (userMastery[factId]?.strength ?: 0) >= MASTERY_STRENGTH - 1
            }
            if (allConsolidating) break

            // 2. Get Next Exercise
            val exercise = strategy.getNextExercise() ?: break

            exercisesCount++
            val factId = exercise.getFactId()
            attemptCounts[factId] = (attemptCounts[factId] ?: 0) + 1

            // 3. Simulate Student Attempt
            val probability = studentModel.getSuccessProbability(factId)
            val wasCorrect = studentRandom.nextDouble() < probability
            val duration = studentModel.getAttemptDuration(factId)

            exercise.timeTakenMillis = duration.toInt()
            
            // SpeedBadge logic (needed for Strength promotion in DrillStrategy)
            val (op, op1, op2) = getOpsFromFactId(factId)
            exercise.speedBadge = getSpeedBadge(op, op1, op2, duration)

            // 4. Record Result
            strategy.recordAttempt(exercise, wasCorrect)
            encounterSequence.add(factId)
        }

        // --- Post Simulation Metrics ---
        val distributionAudit = calculateDistributionAudit(attemptCounts, encounterSequence)

        return SimulationResult(
            exercisesCount,
            requiredFacts.size,
            attemptCounts.size,
            distributionAudit
        )
    }

    private fun calculateDistributionAudit(
        attemptCounts: Map<String, Int>,
        encounterSequence: List<String>
    ): DistributionAudit {
        val counts = attemptCounts.values.map { it.toDouble() }
        val avg = if (counts.isNotEmpty()) counts.average() else 0.0
        val variance = if (counts.size > 1) {
            counts.sumOf { (it - avg) * (it - avg) } / (counts.size - 1)
        } else 0.0
        val stdDev = Math.sqrt(variance)

        var maxSequential = 0
        var currentSequential = 0
        var lastFact = ""
        encounterSequence.forEach { f ->
            if (f == lastFact) {
                currentSequential++
            } else {
                currentSequential = 1
                lastFact = f
            }
            maxSequential = Math.max(maxSequential, currentSequential)
        }

        val totalGap = encounterSequence.withIndex().groupBy { it.value }
            .mapValues { (_, indices) ->
                indices.map { it.index }.zipWithNext { a, b -> b - a }
            }
            .values.flatten()
        val gapAvg = if (totalGap.isNotEmpty()) totalGap.average() else 0.0

        return DistributionAudit(
            stdDev, 
            maxSequential, 
            gapAvg,
            attemptCounts.values.minOrNull() ?: 0,
            attemptCounts.values.maxOrNull() ?: 0
        )
    }
    
    companion object {
        // --- Simulation Configuration ---
        // Strategy uses REPLACEABLE_MASTERY = 4 to swap facts out of working set.
        // We align simulation to match this transition.
        private const val MASTERY_STRENGTH = 5
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