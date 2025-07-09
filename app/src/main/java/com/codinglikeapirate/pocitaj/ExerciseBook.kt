package com.codinglikeapirate.pocitaj

import com.codinglikeapirate.pocitaj.data.ExerciseSource
import com.codinglikeapirate.pocitaj.logic.Exercise

// Holds a set of exercises to do in a session.
// Each Exercise can be checked for correct solution
class ExerciseBook : ExerciseSource {

    private val exercises = mutableListOf<Exercise>()
    private var currentIndex = -1

    override fun initialize(config: com.codinglikeapirate.pocitaj.data.ExerciseConfig) {
        // No-op for the test implementation
    }

    override suspend fun getNextExercise(): Exercise? {
        currentIndex++
        return if (currentIndex < exercises.size) {
            exercises[currentIndex]
        } else {
            null
        }
    }

    override suspend fun recordAttempt(exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        // No-op for the test implementation
    }

    /**
     * Clears the current session and loads a predefined list of exercises for testing.
     * This is the primary way to set up a predictable state for UI tests.
     */
    fun loadSession(predefinedExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(predefinedExercises)
        currentIndex = -1
    }
}
