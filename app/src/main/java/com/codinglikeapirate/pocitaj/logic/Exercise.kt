package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation

/**
 * A simple, in-memory data holder for a generated exercise.
 */
data class Exercise(
    val operand1: Int,
    val operand2: Int,
    val result: Int,
    val operation: Operation
) {
    companion object {
        /**
         * A factory function that creates a specific Exercise from a fact ID string.
         * @param factId The string to parse (e.g., "ADD_5_3").
         */
        fun fromFactId(factId: String): Exercise {
            val parts = factId.split("_")
            val operation = Operation.valueOf(parts[0])
            val op1 = parts[1].toInt()
            val op2 = parts[2].toInt()
            val result = when (operation) {
                Operation.ADDITION -> op1 + op2
                Operation.SUBTRACTION -> op1 - op2
                Operation.MULTIPLICATION -> op1 * op2
                Operation.DIVISION -> op1 / op2
            }
            return Exercise(op1, op2, result, operation)
        }
    }
}