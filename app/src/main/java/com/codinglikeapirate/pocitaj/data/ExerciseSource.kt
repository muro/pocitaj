package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Exercise

/**
 * Defines the configuration for a set of exercises.
 *
 * @param type The type of operation (e.g., "addition", "subtraction").
 * @param difficulty A parameter to control the complexity of exercises (e.g., numbers up to 10).
 * @param count The total number of exercises in the set.
 */
data class ExerciseConfig(val type: String, val difficulty: Int, val count: Int)

enum class ExerciseType(val id: String) {
    ADDITION("addition"),
    SUBTRACTION("subtraction"),
    MULTIPLICATION("multiplication"),
    DIVISION("division")
}

interface ExerciseSource {
    fun initialize(config: ExerciseConfig)
    suspend fun getNextExercise(): Exercise?
    suspend fun recordAttempt(
        exercise: Exercise,
        submittedAnswer: Int,
        durationMs: Long
    )
}
