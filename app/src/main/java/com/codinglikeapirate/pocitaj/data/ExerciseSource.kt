package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Exercise

interface ExerciseSource {
    suspend fun getNextExercise(): Exercise?
    suspend fun recordAttempt(
        exercise: Exercise,
        submittedAnswer: Int,
        durationMs: Long
    )
}
