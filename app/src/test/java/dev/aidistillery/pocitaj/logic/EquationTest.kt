package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Test

class EquationTest {

    // --- Addition ---
    @Test
    fun `Addition formats question and factId correctly`() {
        val eq = Addition(5, 3)
        assertEquals("5 + 3 = ?", eq.question())
        assertEquals("5 + 3 = ?", eq.getFactId())
    }

    @Test
    fun `Addition formats solved state correctly`() {
        val eq = Addition(5, 3)
        assertEquals("5 + 3 = 8", eq.getQuestionAsSolved(8))
        assertEquals("5 + 3 = ?", eq.getQuestionAsSolved(null))
    }

    // --- Subtraction ---
    @Test
    fun `Subtraction formats question and factId correctly`() {
        val eq = Subtraction(10, 4)
        assertEquals("10 - 4 = ?", eq.question())
        assertEquals("10 - 4 = ?", eq.getFactId())
    }

    @Test
    fun `Subtraction formats solved state correctly`() {
        val eq = Subtraction(10, 4)
        assertEquals("10 - 4 = 6", eq.getQuestionAsSolved(6))
    }

    // --- Multiplication ---
    @Test
    fun `Multiplication formats question and factId correctly`() {
        val eq = Multiplication(7, 6)
        assertEquals("7 × 6 = ?", eq.question())
        assertEquals("7 * 6 = ?", eq.getFactId()) // Note: * vs ×
    }

    @Test
    fun `Multiplication formats solved state correctly`() {
        val eq = Multiplication(7, 6)
        assertEquals("7 × 6 = 42", eq.getQuestionAsSolved(42))
    }

    // --- Division ---
    @Test
    fun `Division formats question and factId correctly`() {
        val eq = Division(20, 5)
        assertEquals("20 ÷ 5 = ?", eq.question())
        assertEquals("20 / 5 = ?", eq.getFactId()) // Note: / vs ÷
    }

    @Test
    fun `Division formats solved state correctly`() {
        val eq = Division(20, 5)
        assertEquals("20 ÷ 5 = 4", eq.getQuestionAsSolved(4))
    }

    // --- Missing Addend ---
    @Test
    fun `MissingAddend formats question and factId correctly`() {
        val eq = MissingAddend(5, 12) // 5 + ? = 12
        assertEquals("5 + ? = 12", eq.question())
        assertEquals("5 + ? = 12", eq.getFactId())
    }

    @Test
    fun `MissingAddend formats solved state correctly`() {
        val eq = MissingAddend(5, 12)
        assertEquals("5 + 7 = 12", eq.getQuestionAsSolved(7))
    }

    // --- Missing Subtrahend ---
    @Test
    fun `MissingSubtrahend formats question and factId correctly`() {
        val eq = MissingSubtrahend(10, 4) // 10 - ? = 4
        assertEquals("10 - ? = 4", eq.question())
        assertEquals("10 - ? = 4", eq.getFactId())
    }

    @Test
    fun `MissingSubtrahend formats solved state correctly`() {
        val eq = MissingSubtrahend(10, 4)
        assertEquals("10 - 6 = 4", eq.getQuestionAsSolved(6))
    }

    // --- TwoDigitEquation (Regression Tests) ---
    @Test
    fun `TwoDigitEquation addition formats correctly`() {
        val eq = TwoDigitEquation(Operation.ADDITION, 13, 24, "13 + 24 = ?")
        assertEquals("13 + 24 = ?", eq.question())
        assertEquals("13 + 24 = 37", eq.getQuestionAsSolved(37))
        assertEquals("13 + 24 = ?", eq.getFactId())
    }

    @Test
    fun `TwoDigitEquation subtraction formats correctly`() {
        val eq = TwoDigitEquation(Operation.SUBTRACTION, 23, 14, "23 - 14 = ?")
        assertEquals("23 - 14 = ?", eq.question())
        assertEquals("23 - 14 = 9", eq.getQuestionAsSolved(9))
        assertEquals("23 - 14 = ?", eq.getFactId())
    }
}
