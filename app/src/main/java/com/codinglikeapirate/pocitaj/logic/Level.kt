package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation

/**
 * Represents a single, teachable level in the curriculum.
 * Its primary job is to generate random exercises that conform to its rules.
 */
interface Level {
    val id: String
    val operation: Operation
    fun generateExercise(): Exercise
    fun getAllPossibleFactIds(): List<String>
}