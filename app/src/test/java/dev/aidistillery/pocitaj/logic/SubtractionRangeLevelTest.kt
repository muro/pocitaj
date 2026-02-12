package dev.aidistillery.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubtractionRangeLevelTest {

    @Test
    fun `SubtractionRangeLevel matches SubtractionFrom5 facts`() {
        // SubtractionFrom5: 0..5
        val rangeLevel = Curriculum.SubtractionRangeLevel("TEST_ID", 0, 5)
        val originalLevel = Curriculum.SubtractionFrom5

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `SubtractionRangeLevel matches SubtractionFrom10 facts`() {
        // SubtractionFrom10: 6..10
        val rangeLevel = Curriculum.SubtractionRangeLevel("TEST_ID", 6, 10)
        val originalLevel = Curriculum.SubtractionFrom10

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `SubtractionRangeLevel matches SubtractionFrom20 facts`() {
        // SubtractionFrom20: 11..20
        val rangeLevel = Curriculum.SubtractionRangeLevel("TEST_ID", 11, 20)
        val originalLevel = Curriculum.SubtractionFrom20

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        assertEquals(originalFacts, rangeFacts)
    }

    @Test
    fun `generateExercise produces valid subtraction with non-negative result`() {
        val level = Curriculum.SubtractionRangeLevel("TEST_ID", 5, 10)
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
}
