package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillStrategyTest {

    private val testLevel = object : Level {
        override val id = "TEST_LEVEL"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.DRILL
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = (1..10).map { "ADDITION_1_${it}" }
    }

    @Test
    fun `initial working set is formed correctly`() {
        // ARRANGE
        val userMastery = testLevel.getAllPossibleFactIds().associateWith {
            FactMastery(it, 1, 5, 0)
        }.toMutableMap()
        userMastery.remove("ADDITION_1_1")
        userMastery["ADDITION_1_2"] = FactMastery("ADDITION_1_2", 1, 1, 200L) // L1
        userMastery["ADDITION_1_3"] = FactMastery("ADDITION_1_3", 1, 3, 50L)  // L2 (most overdue)
        userMastery["ADDITION_1_4"] = FactMastery("ADDITION_1_4", 1, 4, 400L)  // L2

        // ACT
        val strategy = DrillStrategy(testLevel, userMastery)
        val workingSet = strategy.getWorkingSet()

        // ASSERT
        assertEquals("Working set should be the target size", 4, workingSet.size)
        assertTrue("Should contain all L1 facts", workingSet.containsAll(listOf("ADDITION_1_1", "ADDITION_1_2")))
        assertTrue("Should be filled with the most overdue L2 fact", workingSet.contains("ADDITION_1_3"))
        assertTrue("Should be filled with an unseen fact", workingSet.any { !userMastery.containsKey(it) })
    }

    @Test
    fun `correct answer to L1 fact promotes it to L2`() {
        // ARRANGE
        val userMastery = mutableMapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 0, 100L) // L1
        )
        val strategy = DrillStrategy(testLevel, userMastery)

        // ACT
        val exercise = strategy.getNextExercise()!!
        strategy.recordAttempt(exercise, true)
        strategy.recordAttempt(exercise, true)
        strategy.recordAttempt(exercise, true)

        // ASSERT
        val newMastery = strategy.getUpdatedMastery()[exercise.getFactId()]!!
        assertTrue("Fact should be promoted to L2", newMastery.strength >= 3)
    }

    @Test
    fun `correct answer to L2 fact in a new session promotes it to L3`() {
        // ARRANGE
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        // All facts are mastered (L3) except for one, which is L2 from a previous session.
        val userMastery = testLevel.getAllPossibleFactIds().associateWith {
            FactMastery(it, 1, 5, 0)
        }.toMutableMap()
        userMastery["ADDITION_1_1"] = FactMastery("ADDITION_1_1", 1, 3, yesterday)

        val strategy = DrillStrategy(testLevel, userMastery)

        // ACT
        // The working set should now contain only the single L2 fact.
        val exercise = strategy.getNextExercise()!!
        assertEquals("ADDITION_1_1", exercise.getFactId())
        strategy.recordAttempt(exercise, true)

        // ASSERT
        val newMastery = strategy.getUpdatedMastery()["ADDITION_1_1"]!!
        assertTrue("Fact should be promoted to L3", newMastery.strength >= 5)
    }

    @Test
    fun `incorrect answer to L2 fact demotes it to L1 and injects into working set`() {
        // ARRANGE
        val userMastery = mutableMapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 4, 100L) // L2
        )
        val strategy = DrillStrategy(testLevel, userMastery)
        val initialWorkingSet = strategy.getWorkingSet().toList()

        // ACT
        val exercise = exerciseFromFactId("ADDITION_1_1")
        strategy.recordAttempt(exercise, false)

        // ASSERT
        val newMastery = strategy.getUpdatedMastery()["ADDITION_1_1"]!!
        assertEquals("Fact should be demoted to L1", 0, newMastery.strength)
        assertTrue("Demoted fact should be injected into the working set", strategy.getWorkingSet().contains("ADDITION_1_1"))
        assertEquals("Working set may temporarily exceed target size", 5, strategy.getWorkingSet().size)
    }

    @Test
    fun `session completes when all facts are L3`() {
        // ARRANGE
        val userMastery = testLevel.getAllPossibleFactIds().associateWith {
            FactMastery(it, 1, 5, 0) // All facts are L3
        }.toMutableMap()
        userMastery["ADDITION_1_10"] = FactMastery("ADDITION_1_10", 1, 4, 0) // One fact is not yet L3

        val strategy = DrillStrategy(testLevel, userMastery)

        // ACT
        val exercise = strategy.getNextExercise()!!
        strategy.recordAttempt(exercise, true)
        strategy.recordAttempt(exercise, true)

        // ASSERT
        assertNull("getNextExercise should return null when all facts are mastered", strategy.getNextExercise())
    }
}

// Helper extension to access internal state for testing
private fun DrillStrategy.getWorkingSet(): List<String> {
    val field = this::class.java.getDeclaredField("workingSet")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as List<String>
}

private fun DrillStrategy.getUpdatedMastery(): Map<String, FactMastery> {
    val field = this::class.java.getDeclaredField("userMastery")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as Map<String, FactMastery>
}