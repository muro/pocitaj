package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation

/**
 * Represents a single, teachable level in the curriculum.
 */
interface Level {
    val id: String
    val operation: Operation
    fun generateExercise(): Exercise
}
