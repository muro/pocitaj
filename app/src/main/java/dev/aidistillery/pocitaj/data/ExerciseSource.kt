package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.ExerciseStrategy

/**
 * Defines the configuration for a set of exercises.
 *
 * @param operation The type of operation (e.g., "addition", "subtraction").
 * @param difficulty A parameter to control the complexity of exercises (e.g., numbers up to 10).
 * @param count The total number of exercises in the set.
 */
data class ExerciseConfig(
    val operation: Operation,
    val difficulty: Int,
    val count: Int,
    val levelId: String? = null,
    val strategy: ExerciseStrategy = ExerciseStrategy.SMART_PRACTICE
)

interface ExerciseSource {
    suspend fun initialize(config: ExerciseConfig)
    fun getNextExercise(): Exercise?
    suspend fun recordAttempt(
        exercise: Exercise,
        submittedAnswer: Int,
        durationMs: Long
    )
}
