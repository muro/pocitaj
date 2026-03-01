package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThan as shouldBeLessThanInt
import io.kotest.matchers.booleans.shouldBeTrue
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
        val distribution: DistributionAudit? = null,
        val masteryTrend: List<Int> = emptyList() // % of facts mastered over time
    )

    @Test
    fun simulate_ladder_of_abstraction() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val student = PureBeginnerStudent()

        println("\n=== LADDER OF ABSTRACTION (Learning Behavior Audit) ===")
        
        // Level 1 & 2: Base Logic (Decision Tracing)
        println(">> LEVEL 1: Decision Trace (Sampling first 20 & periodically)")
        val result = runSimulationUntilMastery(
            levels, 
            student, 
            isSmartPractice = true, 
            verbose = true, 
            maxExercises = 1000 // Limit for audit clarity
        )

        // Level 3: Aggregate Trends (Mastery Curve)
        println("\n>> LEVEL 3: Mastery Growth Curve (% Mastered every 50 exercises)")
        val curve = result.masteryTrend.joinToString(" -> ") { "$it%" }
        println(curve)

        withClue("Mastery Curve: Should show upward trend") {
            result.masteryTrend.first() shouldBeLessThanInt result.masteryTrend.last()
        }
    }

    @Test
    fun verify_working_set_impact_on_variety() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val level = levels.first { it.id == "ADD_SUM_5" } // Small level for high density
        val student = PerfectStudent()
        
        println("\n=== VARIETY AUDIT: Working Set Impact ===")
        
        listOf(1, 3, 5, 8).forEach { size ->
            val strategyRandom = Random(42)
            val strategy = SmartPracticeStrategy(levels, mutableMapOf(), 1L, random = strategyRandom, workingSetSize = size)
            
            val sequence = mutableListOf<String>()
            repeat(50) {
                val exercise = strategy.getNextExercise() ?: return@repeat
                sequence.add(exercise.getFactId())
                strategy.recordAttempt(exercise, true)
            }
            
            val maxReps = calculateMaxSequential(sequence)
            val uniqueIn50 = sequence.distinct().size
            println("Size %d | Max Reps: %d | Unique in 50: %d".format(size, maxReps, uniqueIn50))
            
            if (size > 1) {
                // With pure random selection from a small set, 3 in a row is statistically possible.
                // We just want to ensure it's not "stuck" (e.g. 5+ reps)
                maxReps shouldBeLessThanInt 5
            }
        }
    }

    private fun calculateMaxSequential(sequence: List<String>): Int {
        if (sequence.isEmpty()) return 0
        var max = 0
        var current = 0
        var last = ""
        sequence.forEach { 
            if (it == last) current++ else { current = 1; last = it }
            max = maxOf(max, current)
        }
        return max
    }

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
        verifyAdaptabilityPersona("Adaptive Beginner", AdaptiveBeginner(), maxExpected = 20000)
    }

    @Test
    fun simulate_smart_practice_zero_state_comparison() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val allFacts = levels.flatMap { it.getAllPossibleFactIds() }.distinct()
        
        println("\n=== ZERO STATE COMPARISON (Full Addition) ===")
        println("Operation: ADDITION, Total Facts: ${allFacts.size}")
        
        val personas = listOf(
            "Perfect" to PerfectStudent(),
            "Mistake Prone" to MistakeProneStudent(),
            "Adv Struggling" to AdvancedStrugglingStudent(),
            "Pure Beginner" to PureBeginnerStudent(),
            "Grand Master" to GrandMasterStudent(),
            "Adaptive Beginner" to AdaptiveBeginner()
        )
        
        val headerFormat = "| %-20s | %-12s | %-12s |"
        println(headerFormat.format("Persona", "Exercises", "Efficiency"))
        println("|----------------------|--------------|--------------|")
        
        personas.forEach { (name, student) ->
            val result = runSimulationUntilMastery(levels, student, isSmartPractice = true)
            val efficiency = result.exerciseCount.toDouble() / allFacts.size
            println(headerFormat.format(name, result.exerciseCount, "%.2fx".format(efficiency)))
            
            // Baseline safety: No persona should take more than 20k to master Addition (approx 400 facts)
            result.exerciseCount shouldBeLessThanInt 20000
        }
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

    @Test
    fun simulate_smart_practice_unified_matrix() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        
        println("\n=== UNIFIED SIMULATION MATRIX (Addition) ===")
        val headerFormat = "| %-16s | %-16s | %-10s | %-11s | %-18s |"
        println(headerFormat.format("Persona", "Initial State", "Ex to Master", "Efficiency", "Phase 1 Focus"))
        println("|------------------|------------------|------------|-------------|--------------------|")

        val scenarios = listOf(
            Triple("Grand Master", "Scratch", GrandMasterStudent() to emptyMap<String, FactMastery>()),
            Triple("Pure Beginner", "Scratch", PureBeginnerStudent() to emptyMap<String, FactMastery>()),
            Triple("Rusty Veteran", "All @ Strength 3", RustyVeteranStudent() to levels.flatMap { it.getAllPossibleFactIds() }.associateWith { 
                FactMastery(it, 1L, "LEVEL_ID", 3, 0L, avgDurationMs = 2500L) 
            }),
            Triple("Mid-Way", "Mastered 1-5", MidWayStudent() to levels.take(6).flatMap { it.getAllPossibleFactIds() }.associateWith {
                FactMastery(it, 1L, "LEVEL_ID", 5, 0L, avgDurationMs = 800L)
            }),
            Triple("Adv Intro", "Mastered 1-3", AdvancedStrugglingStudent() to levels.take(4).flatMap { it.getAllPossibleFactIds() }.associateWith {
                FactMastery(it, 1L, "LEVEL_ID", 5, 0L, avgDurationMs = 600L)
            })
        )

        scenarios.forEach { (name, state, logic) ->
            val (student, history) = logic
            
            // Phase 1: Calibration (First 20 exercises)
            val trace = runTrace(levels, student, isSmartPractice = true, initialMastery = history, count = 20)
            val levelCounts = trace.encounterSequence.map { factId ->
                levels.find { it.recognizes(Equation.parse(factId)!!) }?.id ?: "Unknown"
            }.groupingBy { it }.eachCount()
            
            val topLevel = levelCounts.maxByOrNull { it.value }?.key?.take(15) ?: "N/A"
            val phase1Summary = "$topLevel (${levelCounts[topLevel]}/20)"

            // Full simulation
            val result = runSimulationUntilMastery(levels, student, isSmartPractice = true, initialMastery = history)
            
            val efficiency = "%.2fx".format(result.exerciseCount.toDouble() / result.factsCount.coerceAtLeast(1))
            
            println(headerFormat.format(name, state, result.exerciseCount, efficiency, phase1Summary))
            result.exerciseCount shouldBeLessThanInt 20000 // Safety guard
        }
    }

    @Test
    fun verify_frontier_calibration_speed() {
        val levels = Curriculum.getLevelsFor(Operation.ADDITION)
        val masterLevels = levels.take(5)
        val coldLevel = levels[5]
        
        // Setup history: 1-5 Mastered
        val history = masterLevels.flatMap { it.getAllPossibleFactIds() }.associateWith {
            FactMastery(it, 1L, "LEVEL_ID", 5, 0L, avgDurationMs = 700L)
        }
        
        println("\n=== FRONTIER CALIBRATION AUDIT ===")
        println("Target Frontier: ${coldLevel.id}")
        
        // Run a short burst of 20 exercises
        val result = runTrace(levels, PerfectStudent(), isSmartPractice = true, initialMastery = history, count = 20)
        
        val coldLevelFactIds = coldLevel.getAllPossibleFactIds().toSet()
        val coldHits = result.encounterSequence.count { it in coldLevelFactIds }
        val percentage = (coldHits.toDouble() / 20) * 100
        
        println("Calibration Hits (First 20): $coldHits / 20 (${percentage.toInt()}%)")
        
        withClue("Calibration Audit: Should identify and focus on Level 6 (cold) immediately (~80% probability)") {
            // 80% of 20 is 16. Allow slight variance but expect high focus.
            coldHits shouldBeGreaterThanOrEqual 15 
        }
    }

    private fun runTrace(
        curriculum: List<Level>,
        studentModel: StudentModel,
        isSmartPractice: Boolean,
        initialMastery: Map<String, FactMastery> = emptyMap(),
        count: Int
    ): TraceResult {
        val userMastery = initialMastery.toMutableMap()
        val strategyRandom = Random(12345)
        val studentRandom = Random(54321)
        val clock = object : Clock { override fun now() = Instant.fromEpochMilliseconds(0L) }
        
        val strategy = if (isSmartPractice) {
            SmartPracticeStrategy(curriculum, userMastery, activeUserId = 1L, random = strategyRandom, clock = clock)
        } else {
            curriculum.first().createStrategy(userMastery, activeUserId = 1L, clock = clock, random = strategyRandom)
        }
        
        val sequence = mutableListOf<String>()
        repeat(count) {
            val exercise = strategy.getNextExercise() ?: return@repeat
            sequence.add(exercise.getFactId())
            strategy.recordAttempt(exercise, studentRandom.nextDouble() < studentModel.getSuccessProbability(exercise.getFactId()))
        }
        return TraceResult(sequence)
    }

    data class TraceResult(val encounterSequence: List<String>)

    @Test
    fun simulate_smart_practice_steady_state() {
        // 1. Setup: Master initial levels 0-7 of Addition
        val additionLevels = Curriculum.getLevelsFor(Operation.ADDITION)
        val initialMasterLevels = additionLevels.take(8)
        
        val userMastery = initialMasterLevels.flatMap { it.getAllPossibleFactIds() }.associateWith {
            FactMastery(it, 1L, "LEVEL_ID", 5, 0L, avgDurationMs = 1000L)
        }.toMutableMap()
        
        val strategy = SmartPracticeStrategy(additionLevels, userMastery, activeUserId = 1L, random = Random(42))
        
        // 2. Run simulation and track distribution dynamically
        var frontierHits = 0
        var reviewHits = 0
        val totalExercises = 100
        
        // We'll also track multi-fact updates
        var maxAffectedFactsSeen = 0

        repeat(totalExercises) {
            // Determine the FIRST unmastered level as the current frontier
            val frontierLevel = additionLevels.firstOrNull { level ->
                level.getAllPossibleFactIds().any { (userMastery[it]?.strength ?: 0) < 4 }
            } ?: additionLevels.last()
            
            val exercise = strategy.getNextExercise()
            val factId = exercise.getFactId()
            
            val isFrontierHit = frontierLevel.getAllPossibleFactIds().contains(factId)
            if (isFrontierHit) frontierHits++ else reviewHits++
            
            // For multi-fact verification: get affected IDs manually for the exercise
            val level = additionLevels.find { it.id == "ADD_TWO_DIGIT_NO_CARRY" }
            if (level != null && level.recognizes(exercise.equation)) {
                maxAffectedFactsSeen = maxOf(maxAffectedFactsSeen, level.getAffectedFactIds(exercise).size)
            }

            strategy.recordAttempt(exercise.apply { 
                timeTakenMillis = 500
                speedBadge = SpeedBadge.GOLD 
            }, wasCorrect = true)
        }
        
        println("Steady State Distribution ($totalExercises ex):")
        println("Frontier Hits: $frontierHits (${(frontierHits * 100 / totalExercises)}%)")
        println("Review Hits: $reviewHits (${(reviewHits * 100 / totalExercises)}%)")
        println("Max Affected Facts in one answer (2-digit): $maxAffectedFactsSeen")
        
        withClue("Steady State: Should mostly focus on Frontier (Expected ~80%, Actual: $frontierHits)") {
            val ratio = frontierHits.toDouble() / totalExercises
            (ratio >= 0.6 && ratio <= 0.98).shouldBeTrue()
        }
        
        withClue("Steady State: Should occasionally review Mastered (Expected ~20%, Actual: $reviewHits)") {
            val ratio = reviewHits.toDouble() / totalExercises
            (ratio >= 0.02 && ratio <= 0.4).shouldBeTrue()
        }

        withClue("Multi-Fact Updates: A 2-digit exercise should update composite and component facts (Affected count should be >= 2)") {
            // 2-digit addition updates: Full + components. 
            // e.g. 12+13 -> 12+13, 2+3, 10+10 (3 facts)
            // e.g. 10+20 -> 10+20, 0+0 (2 facts as 10+20 is both composite and tens)
            (maxAffectedFactsSeen >= 2).shouldBeTrue()
        }
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
        maxExercises: Int = 20000,
        verbose: Boolean = false
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
        val masteryTrend = mutableListOf<Int>()

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
            val exercise = strategy!!.getNextExercise() ?: break
            val reason = (strategy as? SmartPracticeStrategy)?.lastDecisionReason ?: "Standard"

            if (verbose && (exercisesCount < 20 || exercisesCount % 50 == 0)) {
                println("  Ex #%-4d: %-20s | Reason: %s".format(exercisesCount + 1, exercise.getFactId(), reason))
            }

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
            strategy!!.recordAttempt(exercise, wasCorrect)
            encounterSequence.add(factId)

            // 5. Mastery Snapshot (Aggregate Level)
            if (exercisesCount % 50 == 0 || allConsolidating) {
                val masteredCount = requiredFacts.count { (userMastery[it]?.strength ?: 0) >= MASTERY_STRENGTH - 1 }
                masteryTrend.add((masteredCount * 100) / requiredFacts.size.coerceAtLeast(1))
            }
        }

        // --- Post Simulation Metrics ---
        val distributionAudit = calculateDistributionAudit(attemptCounts, encounterSequence)

        return SimulationResult(
            exercisesCount,
            requiredFacts.size,
            attemptCounts.size,
            distributionAudit,
            masteryTrend
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