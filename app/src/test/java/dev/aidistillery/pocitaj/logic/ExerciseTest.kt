package dev.aidistillery.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseTest {

    @Test
    fun `getFactId creates correct addition exercise`() {
        val exercise = Exercise(Addition(5, 3))
        assertEquals("ADDITION_5_3", exercise.getFactId())
    }

    @Test
    fun `getFactId creates correct subtraction exercise`() {
        val exercise = Exercise(Subtraction(10, 4))
        assertEquals("SUBTRACTION_10_4", exercise.getFactId())
    }

    @Test
    fun `getFactId creates correct multiplication exercise`() {
        val exercise = Exercise(Multiplication(7, 8))
        assertEquals("MULTIPLICATION_7_8", exercise.getFactId())
    }

    @Test
    fun `getFactId handles zero values`() {
        val exercise = Exercise(Addition(0, 0))
        assertEquals("ADDITION_0_0", exercise.getFactId())
    }
}