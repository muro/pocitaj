package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `getAllLevels returns correct number of levels`() {
        val levels = Curriculum.getAllLevels()
        // 3 basic levels + 13 multiplication (0-12) + 12 division (1-12)
        val expectedCount = 3 + 13 + 12
        assertEquals(expectedCount, levels.size)
    }

    @Test
    fun `MultiplicationTableLevel generates correct exercises`() {
        val table = 7
        val level = Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Multiplication
            assertTrue("One of the operands must be $table", equation.a == table || equation.b == table)
        }
    }

    @Test
    fun `MultiplicationTableLevel generates correct fact IDs`() {
        val table = 7
        val level = Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        val factIds = level.getAllPossibleFactIds()

        // (0..10) for op2 -> 11 facts. Times 2 for commutativity, but some are duplicates (e.g., 7x7)
        // The set logic handles duplicates, so we check for specific examples.
        assertEquals(21, factIds.size) // 11 pairs, 7x7 is not duplicated
        assertTrue(factIds.contains("MULTIPLICATION_7_0"))
        assertTrue(factIds.contains("MULTIPLICATION_0_7"))
        assertTrue(factIds.contains("MULTIPLICATION_7_10"))
        assertTrue(factIds.contains("MULTIPLICATION_10_7"))
        assertTrue(factIds.contains("MULTIPLICATION_7_7"))
    }

    @Test
    fun `DivisionTableLevel generates correct exercises`() {
        val divisor = 6
        val level = Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Division
            assertEquals("The divisor must be $divisor", divisor, equation.b)
            assertTrue("The dividend must be divisible by the divisor", equation.a % divisor == 0)
        }
    }

    @Test
    fun `DivisionTableLevel generates correct fact IDs`() {
        val divisor = 6
        val level = Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(11, factIds.size) // 0..10 for the result
        assertTrue(factIds.contains("DIVISION_0_6"))
        assertTrue(factIds.contains("DIVISION_30_6"))
        assertTrue(factIds.contains("DIVISION_60_6"))
    }
}
