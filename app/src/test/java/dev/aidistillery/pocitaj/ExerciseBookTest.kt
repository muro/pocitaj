package dev.aidistillery.pocitaj

import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.MissingAddend
import dev.aidistillery.pocitaj.logic.MissingSubtrahend
import dev.aidistillery.pocitaj.logic.Multiplication
import dev.aidistillery.pocitaj.logic.Subtraction
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.Test


class ExerciseBookTest {

    @Test
    fun addition_Question() {
        val equation = Addition(2, 3)
        equation.question() shouldBe "2 + 3 = ?"
    }

    @Test
    fun subtraction_Question() {
        val equation = Subtraction(5, 3)
        equation.question() shouldBe "5 - 3 = ?"
    }

    @Test
    fun addition_EquationStringBeforeSolving() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        exercise.equationString() shouldBe "2 + 3 = ?"
    }

    @Test
    fun addition_SolveNotRecognized() {
        val equation = Addition(2, 3)
        equation.question() shouldBe "2 + 3 = ?"

        val exercise = Exercise(equation)
        exercise.solve(Exercise.NOT_RECOGNIZED).shouldBeFalse()
        exercise.solved.shouldBeFalse()
        exercise.correct().shouldBeFalse()
        exercise.equationString() shouldBe "2 + 3 = ?"
    }

    @Test
    fun addition_SolveIncorrectly() {
        val equation = Addition(4, 2)
        equation.question() shouldBe "4 + 2 = ?"

        val exercise = Exercise(equation)
        exercise.solve(7).shouldBeFalse()
        exercise.solved.shouldBeTrue()
        exercise.correct().shouldBeFalse()
        exercise.equationString() shouldBe "4 + 2 ≠ 7"
    }

    @Test
    fun addition_SolveCorrectly() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        exercise.solve(5).shouldBeTrue()
        exercise.solved.shouldBeTrue()
        exercise.correct().shouldBeTrue()
        exercise.equationString() shouldBe "2 + 3 = 5"
    }


    // Add tests for the equationString() function
    @Test
    fun `equationString returns correct format for unsolved Addition`() {
        val exercise = Exercise(Addition(5, 3))
        exercise.equationString() shouldBe "5 + 3 = ?"
    }

    @Test
    fun `equationString returns correct format for solved correct Addition`() {
        val exercise = Exercise(Addition(5, 3))
        exercise.solve(8)
        exercise.equationString() shouldBe "5 + 3 = 8"
    }

    @Test
    fun `equationString returns correct format for solved incorrect Addition`() {
        val exercise = Exercise(Addition(5, 3))
        exercise.solve(7)
        exercise.equationString() shouldBe "5 + 3 ≠ 7"
    }

    @Test
    fun `equationString returns correct format for unsolved Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        exercise.equationString() shouldBe "10 - 4 = ?"
    }

    @Test
    fun `equationString returns correct format for solved correct Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        exercise.solve(6)
        exercise.equationString() shouldBe "10 - 4 = 6"
    }

    @Test
    fun `equationString returns correct format for solved incorrect Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        exercise.solve(5)
        exercise.equationString() shouldBe "10 - 4 ≠ 5"
    }

    @Test
    fun `equationString returns correct format for unsolved Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        exercise.equationString() shouldBe "7 × 3 = ?"
    }

    @Test
    fun `equationString returns correct format for solved correct Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        exercise.solve(21)
        exercise.equationString() shouldBe "7 × 3 = 21"
    }

    @Test
    fun `equationString returns correct format for solved incorrect Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        exercise.solve(5)
        exercise.equationString() shouldBe "7 × 3 ≠ 5"
    }

    @Test
    fun `equationString returns correct format for unsolved MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, null, 15))
        exercise.equationString() shouldBe "7 + ? = 15"
    }

    @Test
    fun `equationString returns correct format for solved correct MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, null, 15))
        exercise.solve(8)
        exercise.equationString() shouldBe "7 + 8 = 15"
    }

    @Test
    fun `equationString returns correct format for solved incorrect MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, null, 15))
        exercise.solve(9)
        // For incorrect, it should show the original equation with the incorrect submitted answer
        exercise.equationString() shouldBe "7 + 9 ≠ 15"
    }

    @Test
    fun `equationString returns correct format for unsolved MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        exercise.equationString() shouldBe "20 - ? = 8"
    }

    @Test
    fun `equationString returns correct format for solved correct MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        exercise.solve(12)
        // Expected correct format: operand1 - submittedSolution = result
        exercise.equationString() shouldBe "20 - 12 = 8"
    }

    @Test
    fun `equationString returns correct format for solved incorrect MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        exercise.solve(10)
        // For incorrect, it should show the original equation with the incorrect submitted answer
        exercise.equationString() shouldBe "20 - 10 ≠ 8"
    }
}