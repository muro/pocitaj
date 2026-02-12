package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeLevelTest {

    @Test
    fun `RangeLevel matches SubtractionFrom5 facts`() {
        // SubtractionFrom5: 0..5
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 0, 5)
        val originalLevel = Curriculum.SubtractionFrom5

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `RangeLevel matches SubtractionFrom10 facts`() {
        // SubtractionFrom10: 6..10
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 6, 10)
        val originalLevel = Curriculum.SubtractionFrom10

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `RangeLevel matches SubtractionFrom20 facts`() {
        // SubtractionFrom20: 11..20
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 11, 20)
        val originalLevel = Curriculum.SubtractionFrom20

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `generateExercise produces valid subtraction with non-negative result`() {
        val level = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 5, 10)
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Subtraction
            val (op, op1, op2) = equation.getFact()

            // Check op1 range
            assertTrue("op1 should be in range 5..10", op1 in 5..10)
            // Check op2 range
            assertTrue("op2 should be in range 0..op1", op2 in 0..op1)
            // Result check
            assertTrue("Result must be non-negative", (op1 - op2) >= 0)
        }
    }

    @Test
    fun `RangeLevel matches SumsUpTo10 facts`() {
        // SumsUpTo10: 6..10
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.ADDITION, 6, 10)
        val originalLevel = Curriculum.SumsUpTo10

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `generateExercise produces valid addition with correct sum range`() {
        val level = Curriculum.RangeLevel("TEST_ID", Operation.ADDITION, 5, 10)
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Addition
            val (op, op1, op2) = equation.getFact()

            // Sum check
            val sum = op1 + op2
            assertTrue("Sum should be in range 5..10", sum in 5..10)
        }
    }
}
