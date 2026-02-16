package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class OperationTest {

    @Test
    fun `Operation ADDITION creates Addition equation`() {
        val eq = Operation.ADDITION.createEquation(15, 20)
        eq.shouldBeInstanceOf<Addition>()
        eq.question() shouldBe "15 + 20 = ?"
    }

    @Test
    fun `Operation SUBTRACTION creates Subtraction equation`() {
        val eq = Operation.SUBTRACTION.createEquation(30, 10)
        eq.shouldBeInstanceOf<Subtraction>()
        eq.question() shouldBe "30 - 10 = ?"
    }

    @Test
    fun `Operation MULTIPLICATION creates Multiplication equation`() {
        val eq = Operation.MULTIPLICATION.createEquation(5, 7)
        eq.shouldBeInstanceOf<Multiplication>()
        eq.question() shouldBe "5 × 7 = ?"
    }

    @Test
    fun `Operation DIVISION creates Division equation`() {
        val eq = Operation.DIVISION.createEquation(50, 5)
        eq.shouldBeInstanceOf<Division>()
        eq.question() shouldBe "50 ÷ 5 = ?"
    }
}
