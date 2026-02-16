package dev.aidistillery.pocitaj.logic

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class EquationTest {

    // --- Addition ---
    @Test
    fun `Addition formats question and factId correctly`() {
        val eq = Addition(5, 3)
        eq.question() shouldBe "5 + 3 = ?"
        eq.getFactId() shouldBe "5 + 3 = ?"
    }

    @Test
    fun `Addition formats solved state correctly`() {
        val eq = Addition(5, 3)
        eq.getQuestionAsSolved(8) shouldBe "5 + 3 = 8"
        eq.getQuestionAsSolved(null) shouldBe "5 + 3 = ?"
    }

    @Test
    fun `TwoDigitEquation addition formats correctly`() {
        val eq = Addition(13, 24)
        eq.question() shouldBe "13 + 24 = ?"
        eq.getFactId() shouldBe "13 + 24 = ?"
        eq.getQuestionAsSolved(37) shouldBe "13 + 24 = 37"
        eq.getQuestionAsSolved(null) shouldBe "13 + 24 = ?"
    }

    // --- Subtraction ---
    @Test
    fun `Subtraction formats question and factId correctly`() {
        val eq = Subtraction(10, 4)
        eq.question() shouldBe "10 - 4 = ?"
        eq.getFactId() shouldBe "10 - 4 = ?"
    }

    @Test
    fun `Subtraction formats solved state correctly`() {
        val eq = Subtraction(10, 4)
        eq.getQuestionAsSolved(6) shouldBe "10 - 4 = 6"
    }

    @Test
    fun `TwoDigitEquation subtraction formats correctly`() {
        val eq = Subtraction(23, 14)
        eq.question() shouldBe "23 - 14 = ?"
        eq.getFactId() shouldBe "23 - 14 = ?"
        eq.getQuestionAsSolved(9) shouldBe "23 - 14 = 9"
        eq.getQuestionAsSolved(null) shouldBe "23 - 14 = ?"
    }

    // --- Multiplication ---
    @Test
    fun `Multiplication formats question and factId correctly`() {
        val eq = Multiplication(7, 6)
        eq.question() shouldBe "7 × 6 = ?"
        eq.getFactId() shouldBe "7 * 6 = ?" // Note: * vs ×
    }

    @Test
    fun `Multiplication formats solved state correctly`() {
        val eq = Multiplication(7, 6)
        eq.getQuestionAsSolved(42) shouldBe "7 × 6 = 42"
    }

    // --- Division ---
    @Test
    fun `Division formats question and factId correctly`() {
        val eq = Division(20, 5)
        eq.question() shouldBe "20 ÷ 5 = ?"
        eq.getFactId() shouldBe "20 / 5 = ?" // Note: / vs ÷
    }

    @Test
    fun `Division formats solved state correctly`() {
        val eq = Division(20, 5)
        eq.getQuestionAsSolved(4) shouldBe "20 ÷ 5 = 4"
    }

    // --- Missing Addend ---
    @Test
    fun `MissingAddend Op1 formats question and factId correctly`() {
        val eq = MissingAddend(null, 5, 12) // ? + 5 = 12
        eq.question() shouldBe "? + 5 = 12"
        eq.getFactId() shouldBe "? + 5 = 12"
    }

    @Test
    fun `MissingAddend Op1 formats solved state correctly`() {
        val eq = MissingAddend(null, 5, 12)
        eq.getQuestionAsSolved(7) shouldBe "7 + 5 = 12"
    }

    @Test
    fun `MissingAddend Op2 formats question and factId correctly`() {
        val eq = MissingAddend(5, null, 12) // 5 + ? = 12
        eq.question() shouldBe "5 + ? = 12"
        eq.getFactId() shouldBe "5 + ? = 12"
    }

    @Test
    fun `MissingAddend Op2 formats solved state correctly`() {
        val eq = MissingAddend(5, null, 12)
        eq.getQuestionAsSolved(7) shouldBe "5 + 7 = 12"
    }

    // --- Missing Subtrahend ---
    @Test
    fun `MissingSubtrahend formats question and factId correctly`() {
        val eq = MissingSubtrahend(10, 4) // 10 - ? = 4
        eq.question() shouldBe "10 - ? = 4"
        eq.getFactId() shouldBe "10 - ? = 4"
    }

    @Test
    fun `MissingSubtrahend formats solved state correctly`() {
        val eq = MissingSubtrahend(10, 4)
        eq.getQuestionAsSolved(6) shouldBe "10 - 6 = 4"
    }

    @Test
    fun `Subtraction handles 2-digit minus 1-digit correctly (21 - 8)`() {
        val eq = Subtraction(21, 8)
        eq.question() shouldBe "21 - 8 = ?"
        eq.getExpectedResult() shouldBe 13
        eq.getFactId() shouldBe "21 - 8 = ?"
    }

    // --- Parsing ---
    @Test
    fun `parse Addition correctly`() {
        val eq = Equation.parse("5 + 3 = ?")
        eq.shouldBeInstanceOf<Addition>()
        eq.getExpectedResult() shouldBe 8
    }

    @Test
    fun `parse Subtraction correctly`() {
        val eq = Equation.parse("10 - 4 = ?")
        eq.shouldBeInstanceOf<Subtraction>()
        eq.getExpectedResult() shouldBe 6
    }

    @Test
    fun `parse Multiplication correctly`() {
        val eq = Equation.parse("7 * 6 = ?")
        eq.shouldBeInstanceOf<Multiplication>()
        eq.getExpectedResult() shouldBe 42
    }

    @Test
    fun `parse Division correctly`() {
        val eq = Equation.parse("20 / 5 = ?")
        eq.shouldBeInstanceOf<Division>()
        eq.getExpectedResult() shouldBe 4
    }

    @Test
    fun `parse MissingAddend correctly`() {
        val eq2 = Equation.parse("5 + ? = 12")
        eq2.shouldBeInstanceOf<MissingAddend>()
        eq2.getExpectedResult() shouldBe 7
        eq2.a shouldBe 5
        eq2.b shouldBe null

        val eq1 = Equation.parse("? + 5 = 12")
        eq1.shouldBeInstanceOf<MissingAddend>()
        eq1.getExpectedResult() shouldBe 7
        eq1.a shouldBe null
        eq1.b shouldBe 5
    }

    @Test
    fun `parse MissingSubtrahend correctly`() {
        val eq = Equation.parse("10 - ? = 4")
        eq.shouldBeInstanceOf<MissingSubtrahend>()
        eq.getExpectedResult() shouldBe 6
    }

    @Test
    fun `parse handles 2-digit numbers correctly`() {
        val eq = Equation.parse("19 + 8 = ?")
        eq.shouldBeInstanceOf<Addition>()
        eq.getExpectedResult() shouldBe 27
    }

    @Test
    fun `parse handles two 2-digit numbers correctly`() {
        val eq = Equation.parse("19 + 18 = ?")
        eq.shouldBeInstanceOf<Addition>()
        eq.getExpectedResult() shouldBe 37
    }

    @Test
    fun `parse returns null for invalid format`() {
        val eq = Equation.parse("invalid")
        eq shouldBe null
    }
}
