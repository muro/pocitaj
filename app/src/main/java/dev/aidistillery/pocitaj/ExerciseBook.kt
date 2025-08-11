package dev.aidistillery.pocitaj

import dev.aidistillery.pocitaj.data.DataStoreActiveUserManager
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Holds a set of exercises to do in a session.
// Each Exercise can be checked for correct solution
class ExerciseBook(
    val exerciseAttemptDao: ExerciseAttemptDao,
    val activeUserManager: DataStoreActiveUserManager
) : ExerciseSource {

    private val exercises = mutableListOf<Exercise>()
    private var currentIndex = -1

    override suspend fun initialize(config: ExerciseConfig) {
        // No-op for the test implementation
    }

    override fun getNextExercise(): Exercise? {
        currentIndex++
        return if (currentIndex < exercises.size) {
            exercises[currentIndex]
        } else {
            null
        }
    }

    override suspend fun recordAttempt(exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        val wasCorrect = exercise.equation.getExpectedResult() == submittedAnswer
        withContext(Dispatchers.IO) {
            val attempt = ExerciseAttempt(
                userId = activeUserManager.activeUser.id,
                timestamp = System.currentTimeMillis(),
                problemText = exercise.equation.question(),
                logicalOperation = exercise.getFactId()
                    .split("_")[0].let { Operation.valueOf(it) },
                correctAnswer = exercise.equation.getExpectedResult(),
                submittedAnswer = submittedAnswer,
                wasCorrect = wasCorrect,
                durationMs = durationMs
            )
            exerciseAttemptDao.insert(attempt)
        }
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
