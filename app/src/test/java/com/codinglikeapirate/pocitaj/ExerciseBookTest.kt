package com.codinglikeapirate.pocitaj

import com.codinglikeapirate.pocitaj.logic.Addition
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.MissingAddend
import com.codinglikeapirate.pocitaj.logic.MissingSubtrahend
import com.codinglikeapirate.pocitaj.logic.Multiplication
import com.codinglikeapirate.pocitaj.logic.Subtraction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ExerciseBookTest {

    @Test
    fun addition_Question() {
        val equation = Addition(2, 3)
        assertEquals("2 + 3 = ?", equation.question())
    }

    @Test
    fun subtraction_Question() {
        val equation = Subtraction(5, 3)
        assertEquals("5 - 3 = ?", equation.question())
    }

    @Test
    fun addition_EquationStringBeforeSolving() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        assertEquals("2 + 3 = ?", exercise.equationString())
    }

    @Test
    fun addition_SolveNotRecognized() {
        val equation = Addition(2, 3)
        assertEquals("2 + 3 = ?", equation.question())

        val exercise = Exercise(equation)
        assertFalse(exercise.solve(Exercise.NOT_RECOGNIZED))
        assertFalse(exercise.solved)
        assertFalse(exercise.correct())
        assertEquals("2 + 3 = ?", exercise.equationString())
    }

    @Test
    fun addition_SolveIncorrectly() {
        val equation = Addition(4, 2)
        assertEquals("4 + 2 = ?", equation.question())

        val exercise = Exercise(equation)
        assertFalse(exercise.solve(7))
        assertTrue(exercise.solved)
        assertFalse(exercise.correct())
        assertEquals("4 + 2 ≠ 7", exercise.equationString())
    }

    @Test
    fun addition_SolveCorrectly() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        assertTrue(exercise.solve(5))
        assertTrue(exercise.solved)
        assertTrue(exercise.correct())
        assertEquals("2 + 3 = 5", exercise.equationString())
    }


    // Add tests for the equationString() function
    @Test
    fun `equationString returns correct format for unsolved Addition`() {
        val exercise = Exercise(Addition(5, 3))
        assertEquals("5 + 3 = ?", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved correct Addition`() {
        val exercise = Exercise(Addition(5, 3))
        exercise.solve(8)
        assertEquals("5 + 3 = 8", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved incorrect Addition`() {
        val exercise = Exercise(Addition(5, 3))
        exercise.solve(7)
        assertEquals("5 + 3 ≠ 7", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for unsolved Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        assertEquals("10 - 4 = ?", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved correct Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        exercise.solve(6)
        assertEquals("10 - 4 = 6", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved incorrect Subtraction`() {
        val exercise = Exercise(Subtraction(10, 4))
        exercise.solve(5)
        assertEquals("10 - 4 ≠ 5", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for unsolved Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        assertEquals("7 × 3 = ?", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved correct Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        exercise.solve(21)
        assertEquals("7 × 3 = 21", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved incorrect Multiplication`() {
        val exercise = Exercise(Multiplication(7, 3))
        exercise.solve(5)
        assertEquals("7 × 3 ≠ 5", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for unsolved MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, 15))
        assertEquals("7 + ? = 15", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved correct MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, 15))
        exercise.solve(8)
        assertEquals("7 + 8 = 15", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved incorrect MissingAddend`() {
        val exercise = Exercise(MissingAddend(7, 15))
        exercise.solve(9)
        // For incorrect, it should show the original equation with the incorrect submitted answer
        assertEquals("7 + 9 ≠ 15", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for unsolved MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        assertEquals("20 - ? = 8", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved correct MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        exercise.solve(12)
        // Expected correct format: operand1 - submittedSolution = result
        assertEquals("20 - 12 = 8", exercise.equationString())
    }

    @Test
    fun `equationString returns correct format for solved incorrect MissingSubtrahend`() {
        val exercise = Exercise(MissingSubtrahend(20, 8))
        exercise.solve(10)
        // For incorrect, it should show the original equation with the incorrect submitted answer
        assertEquals("20 - 10 ≠ 8", exercise.equationString())
    }
}