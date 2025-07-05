package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseTest {

    @Test
    fun `fromFactId creates correct addition exercise`() {
        val exercise = Exercise.fromFactId("ADDITION_5_3")
        assertEquals(5, exercise.operand1)
        assertEquals(3, exercise.operand2)
        assertEquals(8, exercise.result)
        assertEquals(Operation.ADDITION, exercise.operation)
    }

    @Test
    fun `fromFactId creates correct subtraction exercise`() {
        val exercise = Exercise.fromFactId("SUBTRACTION_10_4")
        assertEquals(10, exercise.operand1)
        assertEquals(4, exercise.operand2)
        assertEquals(6, exercise.result)
        assertEquals(Operation.SUBTRACTION, exercise.operation)
    }

    @Test
    fun `fromFactId creates correct multiplication exercise`() {
        val exercise = Exercise.fromFactId("MULTIPLICATION_7_8")
        assertEquals(7, exercise.operand1)
        assertEquals(8, exercise.operand2)
        assertEquals(56, exercise.result)
        assertEquals(Operation.MULTIPLICATION, exercise.operation)
    }

    @Test
    fun `fromFactId creates correct division exercise`() {
        val exercise = Exercise.fromFactId("DIVISION_20_5")
        assertEquals(20, exercise.operand1)
        assertEquals(5, exercise.operand2)
        assertEquals(4, exercise.result)
        assertEquals(Operation.DIVISION, exercise.operation)
    }

    @Test
    fun `fromFactId handles zero values`() {
        val exercise = Exercise.fromFactId("ADDITION_0_0")
        assertEquals(0, exercise.operand1)
        assertEquals(0, exercise.operand2)
        assertEquals(0, exercise.result)
        assertEquals(Operation.ADDITION, exercise.operation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromFactId throws for invalid operation`() {
        Exercise.fromFactId("INVALID_5_3")
    }

    @Test(expected = NumberFormatException::class)
    fun `fromFactId throws for invalid number`() {
        Exercise.fromFactId("ADDITION_A_3")
    }
}
