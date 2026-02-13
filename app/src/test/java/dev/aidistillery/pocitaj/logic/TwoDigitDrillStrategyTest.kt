package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoDigitDrillStrategyTest {
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
        val twoDigitAdditionLevel = TwoDigitComputationLevel(
            "ADD_TWO_DIGIT_CARRY",
            Operation.ADDITION,
            withRegrouping = true
        )

        // Provide some initial mastery for the underlying "tens" and "ones" facts.
        // Logic: op1=19, op2=19 -> Ones: 9+9 (ADD_ONES_9_9). Tens: 1+1 (ADD_TENS_1_1).
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_2_4", 1, 100L),
            createMastery("ADD_ONES_3_7", 2, 200L),
            createMastery("ADD_TENS_1_1", 0, 100L),
            createMastery("ADD_ONES_9_9", 0, 200L) // Weakest combination (Avg 0)
        )

        // ACT: Create the TwoDigitDrillStrategy.
        val strategy =
            TwoDigitDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)

        // ASSERT: The working set should contain Semantic IDs.
        // We expect "19 + 19 = ?" or similar based on weak components.
        assertEquals(4, strategy.workingSet.size)
        // Check for 19+19
        assertTrue(strategy.workingSet.any { it == "19 + 19 = ?" })
    }

    @Test
    fun `getNextExercise for two-digit addition returns a valid exercise`() {
        // ARRANGE
        val twoDigitAdditionLevel = TwoDigitComputationLevel(
            "ADD_TWO_DIGIT_CARRY",
            Operation.ADDITION,
            withRegrouping = true
        )
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_1_2", 1, 100L),
            createMastery("ADD_ONES_2_1", 2, 200L)
        )
        val strategy =
            TwoDigitDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)
        strategy.workingSet.clear()
        strategy.workingSet.add("12 + 21 = ?")

        // ACT
        val exercise = strategy.getNextExercise()

        // ASSERT
        assertTrue(exercise?.equation is TwoDigitEquation)
        val twoDigitEquation = exercise!!.equation as TwoDigitEquation
        val (op, op1, op2) = twoDigitEquation.getFact()
        assertEquals(12, op1)
        assertEquals(21, op2)
        assertEquals(Operation.ADDITION, op)
        assertEquals("12 + 21 = ?", twoDigitEquation.getFactId())
    }

    @Test
    fun `recordAttempt for two-digit addition updates underlying facts`() {
        // ARRANGE
        val twoDigitAdditionLevel = TwoDigitComputationLevel(
            "ADD_TWO_DIGIT_CARRY",
            Operation.ADDITION,
            withRegrouping = true
        )
        val userMastery = mutableMapOf(
            createMastery("ADD_TENS_1_2", 1, 100L),
            createMastery("ADD_ONES_2_1", 2, 200L)
        )
        val strategy =
            TwoDigitDrillStrategy(twoDigitAdditionLevel, userMastery, activeUserId = 1L)
        val exercise =
            Exercise(TwoDigitEquation(Operation.ADDITION, 12, 21, "12 + 21 = ?"))
        // Set a fast time to ensure a SpeedBadge (Gold/Silver) is earned, allowing promotion from Strength 2 -> 3
        exercise.solve(33, timeMillis = 500)

        // ACT
        strategy.recordAttempt(exercise, wasCorrect = true)

        // ASSERT
        assertEquals(2, userMastery["ADD_TENS_1_2"]!!.strength)
        assertEquals(3, userMastery["ADD_ONES_2_1"]!!.strength)
    }

    @Test
    fun `getNextExercise for two-digit subtraction no-borrow returnsValidExercise`() {
        // ARRANGE
        val level = TwoDigitComputationLevel(
            "SUB_TWO_DIGIT_NO_BORROW",
            Operation.SUBTRACTION,
            withRegrouping = false
        )
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = TwoDigitDrillStrategy(level, userMastery, activeUserId = 1L)

        strategy.workingSet.clear()
        // Fact ID for 24 - 13: 24 - 13 = ?
        strategy.workingSet.add("24 - 13 = ?")

        // ACT
        val exercise = strategy.getNextExercise()

        // ASSERT
        assertTrue(exercise?.equation is TwoDigitEquation)
        val equation = exercise!!.equation as TwoDigitEquation
        val (op, op1, op2) = equation.getFact()

        assertEquals(24, op1)
        assertEquals(13, op2)
        assertEquals(Operation.SUBTRACTION, op)
    }

    @Test
    fun `getNextExercise for two-digit subtraction with borrow returnsValidExercise`() {
        // ARRANGE
        val level = TwoDigitComputationLevel(
            "SUB_TWO_DIGIT_BORROW",
            Operation.SUBTRACTION,
            withRegrouping = true
        )
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = TwoDigitDrillStrategy(level, userMastery, activeUserId = 1L)

        strategy.workingSet.clear()
        // Fact ID for 24 - 19: 24 - 19 = ?
        strategy.workingSet.add("24 - 19 = ?")


        // ACT
        val exercise = strategy.getNextExercise()

        // ASSERT
        assertTrue(exercise?.equation is TwoDigitEquation)
        val equation = exercise!!.equation as TwoDigitEquation
        val (op, op1, op2) = equation.getFact()

        // logic reconstruction:
        // op1OnesOrTeens (parts[2]) = 14
        // op2Ones (parts[3]) = 9
        // op1TensEffective (parts[6]) = 1
        // op2Tens (parts[7]) = 1
        //
        // op2 = 1*10 + 9 = 19
        //
        // isAddition = false.
        // op1OnesOrTeens >= 10 (14 >= 10) -> True
        // onesDigit = 14 - 10 = 4
        // originalTens = 1 + 1 = 2
        // op1 = 2*10 + 4 = 24

        assertEquals(24, op1)
        assertEquals(19, op2)
        assertEquals(Operation.SUBTRACTION, op)
    }
}
