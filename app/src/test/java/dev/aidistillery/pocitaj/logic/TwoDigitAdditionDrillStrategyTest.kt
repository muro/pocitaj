package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoDigitAdditionDrillStrategyTest {
    private fun createMastery(
        factId: String,
        strength: Int,
        lastTested: Long
    ): Pair<String, FactMastery> {
        return factId to FactMastery(factId, 1, "", strength, lastTested)
    }

    @Test
    fun `initial working set for two-digit addition is populated with ephemeral tokens`() {
        // ARRANGE: Define a two-digit addition level with carry.
        val twoDigitAdditionLevel = TwoDigitAdditionLevel("ADD_TWO_DIGIT_CARRY", withCarry = true)

        // Provide some initial mastery for the underlying "tens" and "ones" facts.
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_2_4", 1, 100L),
            createMastery("ADD_ONES_3_7", 2, 200L),
            createMastery("ADD_TENS_1_1", 5, 100L),
            createMastery("ADD_ONES_9_9", 0, 200L) // Weakest
        )

        // ACT: Create the TwoDigitAdditionDrillStrategy.
        val strategy =
            TwoDigitAdditionDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)

        // ASSERT: The working set should contain ephemeral tokens, sorted by weakness.
        assertEquals(4, strategy.workingSet.size)
        assertTrue(strategy.workingSet.any { it.contains("ADD_ONES_9_9") })
    }

    @Test
    fun `getNextExercise for two-digit addition returns a valid exercise`() {
        // ARRANGE
        val twoDigitAdditionLevel = TwoDigitAdditionLevel("ADD_TWO_DIGIT_CARRY", withCarry = true)
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_1_2", 1, 100L),
            createMastery("ADD_ONES_2_1", 2, 200L)
        )
        val strategy =
            TwoDigitAdditionDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)
        strategy.workingSet.clear()
        strategy.workingSet.add("ADD_ONES_2_1_ADD_TENS_1_2")

        // ACT
        val exercise = strategy.getNextExercise()

        // ASSERT
        assertTrue(exercise?.equation is TwoDigitAddition)
        val twoDigitAddition = exercise!!.equation as TwoDigitAddition
        assertEquals(12, twoDigitAddition.op1)
        assertEquals(21, twoDigitAddition.op2)
        assertEquals("ADD_ONES_2_1_ADD_TENS_1_2", twoDigitAddition.getFactId())
    }

    @Test
    fun `recordAttempt for two-digit addition updates underlying facts`() {
        // ARRANGE
        val twoDigitAdditionLevel = TwoDigitAdditionLevel("ADD_TWO_DIGIT_CARRY", withCarry = true)
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_1_2", 1, 100L),
            createMastery("ADD_ONES_2_1", 2, 200L)
        )
        val strategy =
            TwoDigitAdditionDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)
        val exercise = Exercise(TwoDigitAddition(12, 21, "ADD_ONES_2_1_ADD_TENS_1_2"))

        // ACT
        strategy.recordAttempt(exercise, wasCorrect = true)

        // ASSERT
        assertEquals(2, userMastery["ADD_TENS_1_2"]!!.strength)
        assertEquals(3, userMastery["ADD_ONES_2_1"]!!.strength)
    }
}
