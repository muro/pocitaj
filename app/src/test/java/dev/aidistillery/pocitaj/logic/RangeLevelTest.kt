package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import org.junit.Test

class RangeLevelTest {

    @Test
    fun `RangeLevel matches SubtractionFrom5 facts`() {
        // SubtractionFrom5: 0..5
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 0, 5)
        val originalLevel = Curriculum.SubtractionFrom5

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        rangeFacts shouldBe originalFacts
    }

    @Test
    fun `RangeLevel matches SubtractionFrom10 facts`() {
        // SubtractionFrom10: 6..10
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 6, 10)
        val originalLevel = Curriculum.SubtractionFrom10

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        rangeFacts shouldBe originalFacts
    }

    @Test
    fun `RangeLevel matches SubtractionFrom20 facts`() {
        // SubtractionFrom20: 11..20
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 11, 20)
        val originalLevel = Curriculum.SubtractionFrom20

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        rangeFacts shouldBe originalFacts
    }

    @Test
    fun `generateExercise produces valid subtraction with non-negative result`() {
        val level = Curriculum.RangeLevel("TEST_ID", Operation.SUBTRACTION, 5, 10)
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Subtraction
            val (op, op1, op2) = equation.getFact()

            op shouldBe Operation.SUBTRACTION
            op1 shouldBeInRange 5..10
            op2 shouldBeInRange 0..op1
            (op1 - op2) shouldBeGreaterThanOrEqual 0
        }
    }

    @Test
    fun `RangeLevel matches SumsUpTo10 facts`() {
        // SumsUpTo10: 6..10
        val rangeLevel = Curriculum.RangeLevel("TEST_ID", Operation.ADDITION, 6, 10)
        val originalLevel = Curriculum.SumsUpTo10

        val rangeFacts = rangeLevel.getAllPossibleFactIds().sorted()
        val originalFacts = originalLevel.getAllPossibleFactIds().sorted()

        rangeFacts shouldBe originalFacts
    }

    @Test
    fun `generateExercise produces valid addition with correct sum range`() {
        val level = Curriculum.RangeLevel("TEST_ID", Operation.ADDITION, 5, 10)
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Addition
            val (op, op1, op2) = equation.getFact()

            op shouldBe Operation.ADDITION
            // Sum check
            op1 + op2 shouldBeInRange 5..10
        }
    }
}
