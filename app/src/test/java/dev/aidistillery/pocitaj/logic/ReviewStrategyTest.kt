package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReviewStrategyTest {

    // A standard level with 10 facts for all tests
    private val testLevel = object : Level {
        override val id = "TEST_LEVEL"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.REVIEW // Important: Set strategy to REVIEW
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = (1..10).map { "ADDITION_1_${it}" }
    }

    // --- Helper Functions ---

    private fun createMastery(
        factId: String,
        strength: Int,
        lastTested: Long
    ): Pair<String, FactMastery> {
        return factId to FactMastery(factId, 1, strength, lastTested)
    }

    private fun setupStrategy(
        masteryMap: MutableMap<String, FactMastery>,
        level: Level = testLevel,
        reviewStrength: Int = 5,
        targetStrength: Int = 6,
        userId: Long = 1L
    ): ReviewStrategy {
        return ReviewStrategy(
            level,
            masteryMap,
            reviewStrength = reviewStrength,
            targetStrength = targetStrength,
            activeUserId = userId
        )
    }

    // --- getNextExercise Tests ---

    @Test
    fun `getNextExercise returns a fact when mastery is empty`() {
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = setupStrategy(userMastery)
        val exercise = strategy.getNextExercise()
        assertNotNull("Should return a non-null exercise even with no history", exercise)
    }

    @Test
    fun `prioritizes overdue facts over recently tested facts`() {
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000
        val fiveMinutesAgo = now - 5 * 60 * 1000
        val overdueFactId = "ADDITION_1_1"
        val recentFactId = "ADDITION_1_2"
        val userMastery = mutableMapOf(
            createMastery(overdueFactId, 1, oneWeekAgo),
            createMastery(recentFactId, 4, fiveMinutesAgo)
        )
        val strategy = setupStrategy(userMastery)

        val selections = (1..20).mapNotNull { strategy.getNextExercise()?.getFactId() }
        val overdueSelections = selections.count { it == overdueFactId }

        assertEquals(
            "The overdue fact should be selected in all 20 attempts",
            20,
            overdueSelections
        )
    }

    // --- recordAttempt Tests ---

    @Test
    fun `incorrect answer resets strength to 1`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 7, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = exerciseFromFactId("ADDITION_1_1")

        strategy.recordAttempt(exercise, wasCorrect = false)

        assertEquals(
            "Strength should be reset to 1 on failure",
            1,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test
    fun `correct answer with Gold speed promotes to targetStrength`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 6, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should be promoted to targetStrength",
            7,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test
    fun `correct answer with Gold speed does not promote beyond targetStrength`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 7, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should not exceed targetStrength",
            7,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test
    fun `correct answer with Silver speed demotes strength by 1`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 7, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.SILVER

        strategy.recordAttempt(exercise, true)

        assertEquals("Strength should be demoted by 1", 6, userMastery["ADDITION_1_1"]!!.strength)
    }

    @Test
    fun `correct answer with Bronze speed does not demote below reviewStrength`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 6, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.BRONZE

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should not go below reviewStrength",
            6,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `creating strategy with an empty level throws exception`() {
        val emptyLevel = object : Level by testLevel {
            override fun getAllPossibleFactIds() = emptyList<String>()
        }
        setupStrategy(mutableMapOf(), level = emptyLevel)
    }
}

// Helper function copied from DrillStrategyTest
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