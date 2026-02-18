package dev.aidistillery.pocitaj

import dev.aidistillery.pocitaj.data.ActiveUserManager
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.ExerciseConfig
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.SessionResult
import dev.aidistillery.pocitaj.data.StarProgress
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.exercise.ResultDescription
import dev.aidistillery.pocitaj.ui.exercise.ResultStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Holds a set of exercises to do in a session.
// Each Exercise can be checked for correct solution
class ExerciseBook(
    val exerciseAttemptDao: ExerciseAttemptDao,
    val activeUserManager: ActiveUserManager
) : ExerciseSource {

    private val exercises = mutableListOf<Exercise>()
    private var currentIndex = -1
    override var currentLevelId: String? = null
        private set

    override suspend fun initialize(config: ExerciseConfig) {
        currentLevelId = config.levelId
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
                logicalOperation = exercise.equation.getFact().first,
                correctAnswer = exercise.equation.getExpectedResult(),
                submittedAnswer = submittedAnswer,
                wasCorrect = wasCorrect,
                durationMs = durationMs
            )
            exerciseAttemptDao.insert(attempt)
        }
    }

    override suspend fun getSessionResult(history: List<Exercise>): SessionResult {
        return SessionResult(
            history.map {
                ResultDescription(
                    it.equationString(),
                    ResultStatus.fromBooleanPair(it.solved, it.correct()),
                    it.timeTakenMillis ?: 0,
                    it.speedBadge
                )
            },
            StarProgress(0, 0)
        )
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
