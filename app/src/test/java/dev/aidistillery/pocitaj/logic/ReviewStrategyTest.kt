package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        masteryMap: Map<String, FactMastery>,
        level: Level = testLevel
    ): ReviewStrategy {
        return ReviewStrategy(level, masteryMap.toMutableMap())
    }

    // --- getNextExercise Tests ---

    @Test
    fun `getNextExercise returns a fact when mastery is empty`() {
        val userMastery = emptyMap<String, FactMastery>()
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
        val userMastery = mapOf(
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
    fun `correct answer increases strength and updates timestamp`() {
        // ARRANGE
        val now = System.currentTimeMillis()
        val factId = "ADDITION_1_1"
        val initialStrength = 2
        val userMastery = mutableMapOf(
            factId to FactMastery(factId, 1, initialStrength, now - 100000)
        )
        val strategy = ReviewStrategy(testLevel, userMastery)
        val exercise = exerciseFromFactId(factId)

        // ACT
        strategy.recordAttempt(exercise, wasCorrect = true)

        // ASSERT
        val updatedMastery = userMastery[factId]!!
        assertEquals(initialStrength + 1, updatedMastery.strength)
        assertTrue("Timestamp should be updated to be very recent", now - updatedMastery.lastTestedTimestamp < 1000)
    }

    @Test
    fun `incorrect answer resets strength and updates timestamp`() {
        // ARRANGE
        val now = System.currentTimeMillis()
        val factId = "ADDITION_1_1"
        val userMastery = mutableMapOf(
            factId to FactMastery(factId, 1, 5, now - 100000)
        )
        val strategy = ReviewStrategy(testLevel, userMastery)
        val exercise = exerciseFromFactId(factId)

        // ACT
        strategy.recordAttempt(exercise, wasCorrect = false)

        // ASSERT
        val updatedMastery = userMastery[factId]!!
        assertEquals("Strength should be reset to 1 on failure", 1, updatedMastery.strength)
        assertTrue("Timestamp should be updated to be very recent", now - updatedMastery.lastTestedTimestamp < 1000)
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