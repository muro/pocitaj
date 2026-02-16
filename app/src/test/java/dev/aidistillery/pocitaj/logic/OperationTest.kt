package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationTest {

    @Test
    fun `Operation ADDITION creates Addition equation`() {
        val eq = Operation.ADDITION.createEquation(15, 20)
        assertTrue(eq is Addition)
        assertEquals("15 + 20 = ?", eq.question())
    }

    @Test
    fun `Operation SUBTRACTION creates Subtraction equation`() {
        val eq = Operation.SUBTRACTION.createEquation(30, 10)
        assertTrue(eq is Subtraction)
        assertEquals("30 - 10 = ?", eq.question())
    }

    @Test
    fun `Operation MULTIPLICATION creates Multiplication equation`() {
        val eq = Operation.MULTIPLICATION.createEquation(5, 7)
        assertTrue(eq is Multiplication)
        assertEquals("5 × 7 = ?", eq.question())
    }

    @Test
    fun `Operation DIVISION creates Division equation`() {
        val eq = Operation.DIVISION.createEquation(50, 5)
        assertTrue(eq is Division)
        assertEquals("50 ÷ 5 = ?", eq.question())
    }
}
