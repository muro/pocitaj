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
)
