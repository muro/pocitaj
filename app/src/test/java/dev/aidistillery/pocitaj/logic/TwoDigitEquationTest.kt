package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Test

class TwoDigitEquationTest {

    @Test
    fun `addition formats question consistently`() {
        val eq = TwoDigitEquation(Operation.ADDITION, 10, 20, "id")
        assertEquals("10 + 20 = ?", eq.question())
    }

    @Test
    fun `subtraction formats question consistently`() {
        val eq = TwoDigitEquation(Operation.SUBTRACTION, 20, 10, "id")
        assertEquals("20 - 10 = ?", eq.question())
    }

    @Test
    fun `addition formats solved question correctly`() {
        val eq = TwoDigitEquation(Operation.ADDITION, 10, 20, "id")
        assertEquals("10 + 20 = 30", eq.getQuestionAsSolved(30))
    }

    @Test
    fun `subtraction formats solved question correctly`() {
        val eq = TwoDigitEquation(Operation.SUBTRACTION, 20, 10, "id")
        // This confirms the fix for the bug where it would ignore the user answer
        assertEquals("20 - 10 = 10", eq.getQuestionAsSolved(10))
    }
}
