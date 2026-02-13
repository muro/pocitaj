package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TableLevelTest {
    @Test
    fun `TableLevel multiplication generates correct exercise`() {
        val level = Curriculum.TableLevel(Operation.MULTIPLICATION, 3)
        repeat(50) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Multiplication
            val (op, op1, op2) = equation.getFact()
            assertEquals("ID should match expected format", "MUL_TABLE_3", level.id)
            assertEquals("Operation should be MULTIPLICATION", Operation.MULTIPLICATION, op)

            // Check if one operand is 3
            assertTrue("One operand should be 3", op1 == 3 || op2 == 3)
            // Check if other is in range 2..12
            val other = if (op1 == 3) op2 else op1
            assertTrue("Other operand should be in range 2..12", other in 2..12)
        }
    }

    @Test
    fun `TableLevel division generates correct exercise`() {
        val level = Curriculum.TableLevel(Operation.DIVISION, 4)
        repeat(50) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Division
            val (op, op1, op2) = equation.getFact()
            assertEquals("ID should match expected format", "DIV_BY_4", level.id)
            assertEquals("Operation should be DIVISION", Operation.DIVISION, op)

            // Check divisor is 4
            assertEquals("Divisor should be 4", 4, op2)
            // Check dividend is multiple of 4
            assertTrue("Dividend should be multiple of 4", op1 % 4 == 0)
            // Check result range 2..10
            val result = op1 / op2
            assertTrue("Result should be in range 2..10", result in 2..10)
        }
    }

    @Test
    fun `TableLevel facts match expected set for multiplication`() {
        val level = Curriculum.TableLevel(Operation.MULTIPLICATION, 5)
        val facts = level.getAllPossibleFactIds().toSet()

        // 2..12 -> 11 pairs. Each pair generates 2 IDs (AxB and BxA)
        // However, for op2 = table (5), 5x5 is generated twice but stored once in the Set.
        // So 10 * 2 + 1 = 21 facts expected.
        assertEquals(21, facts.size)
        assertTrue(facts.contains("5 * 2 = ?"))
        assertTrue(facts.contains("2 * 5 = ?"))
    }

    @Test
    fun `TableLevel facts match expected set for division`() {
        val level = Curriculum.TableLevel(Operation.DIVISION, 6)
        val facts = level.getAllPossibleFactIds().toSet()

        // 2..10 -> 9 items. Each item generates 1 fact (Dividend / Divisor)
        // Total 9 facts expected.
        assertEquals(9, facts.size)
        assertTrue(facts.contains("12 / 6 = ?")) // 12 / 6 = 2
        assertTrue(facts.contains("60 / 6 = ?")) // 60 / 6 = 10
    }
}
