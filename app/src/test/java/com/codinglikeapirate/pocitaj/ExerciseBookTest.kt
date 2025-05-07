package com.codinglikeapirate.pocitaj

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ExerciseBookTest {

    @Test
    fun addition_Question() {
        val equation = Addition(2, 3)
        assertEquals("2 + 3", equation.question())
    }

    @Test
    fun subtraction_Question() {
        val equation = Subtraction(5, 3)
        assertEquals("5 - 3", equation.question())
    }

    @Test
    fun addition_EquationBeforeSolving() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        assertEquals("2 + 3", exercise.equation())
    }

    @Test
    fun addition_SolveNotRecognized() {
        val equation = Addition(2, 3)
        assertEquals("2 + 3", equation.question())

        val exercise = Exercise(equation)
        assertFalse(exercise.solve(ExerciseBook.NOT_RECOGNIZED))
        assertFalse(exercise.solved)
        assertFalse(exercise.correct())
        assertEquals("2 + 3 ≠ ?", exercise.equation())
    }

    @Test
    fun addition_SolveIncorrectly() {
        val equation = Addition(4, 2)
        assertEquals("4 + 2", equation.question())

        val exercise = Exercise(equation)
        assertFalse(exercise.solve(7))
        assertTrue(exercise.solved)
        assertFalse(exercise.correct())
        assertEquals("4 + 2 ≠ 7", exercise.equation())
    }

    @Test
    fun addition_SolveCorrectly() {
        val equation = Addition(2, 3)
        val exercise = Exercise(equation)
        assertTrue(exercise.solve(5))
        assertTrue(exercise.solved)
        assertTrue(exercise.correct())
        assertEquals("2 + 3 = 5", exercise.equation())
    }

    @Test
    fun exerciseBook_emptyStats() {
        val exerciseBook = ExerciseBook()
        assertEquals("0 / 0 (0%)", exerciseBook.stats)
    }

    @Test
    fun exerciseBook_allWrongStats() {
        val exerciseBook = ExerciseBook()
        val incorrect = 100
        exerciseBook.generate(ExerciseType.ADDITION)
        exerciseBook.last.solve(incorrect)
        exerciseBook.generate(ExerciseType.ADDITION)
        exerciseBook.last.solve(incorrect)
        assertEquals("0 / 2 (0%)", exerciseBook.stats)
    }

    @Test
    fun exerciseBook_oneSolvedOneUnsolvedWrongStats() {
        val exerciseBook = ExerciseBook()
        val incorrect = 100
        exerciseBook.generate(ExerciseType.ADDITION)
        exerciseBook.last.solve(incorrect)
        exerciseBook.generate(ExerciseType.SUBTRACTION)
        assertEquals("0 / 1 (0%)", exerciseBook.stats)
    }
}