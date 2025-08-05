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
        masteryMap: Map<String, FactMastery>,
        level: Level = testLevel
    ): ReviewStrategy {
        return ReviewStrategy(level, masteryMap.toMutableMap())
    }

    // --- Tests ---

    @Test
    fun `getNextExercise returns a fact when mastery is empty`() {
        // ARRANGE: A user with no history at all.
        val userMastery = emptyMap<String, FactMastery>()
        val strategy = setupStrategy(userMastery)

        // ACT: Request an exercise.
        val exercise = strategy.getNextExercise()

        // ASSERT: The strategy should provide one of the new, unseen facts.
        assertNotNull("Should return a non-null exercise even with no history", exercise)
    }

    @Test
    fun `prioritizes overdue facts over recently tested facts`() {
        // ARRANGE
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000
        val fiveMinutesAgo = now - 5 * 60 * 1000

        val overdueFactId = "ADDITION_1_1"
        val recentFactId = "ADDITION_1_2"

        val userMastery = mapOf(
            // This fact is strength 1 and very overdue. Urgency should be high.
            createMastery(overdueFactId, 1, oneWeekAgo),
            // This fact is strength 4 and was just tested. Urgency should be very low.
            createMastery(recentFactId, 4, fiveMinutesAgo)
        )
        val strategy = setupStrategy(userMastery)

        // ACT: Get the next exercise 20 times and count the selections.
        val selections = (1..20).mapNotNull { strategy.getNextExercise()?.getFactId() }
        val overdueSelections = selections.count { it == overdueFactId }

        // ASSERT: The overdue fact should be selected almost every time.
        // We allow for a small chance of the other fact being picked due to randomness.
        assertEquals(
            "The overdue fact should be selected in all 20 attempts",
            20,
            overdueSelections
        )
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
