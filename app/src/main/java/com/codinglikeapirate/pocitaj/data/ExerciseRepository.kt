package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.ExerciseConfig
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.ExerciseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepository(
    private val factMasteryDao: FactMasteryDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val userDao: UserDao
) {

    private var activeOperation: Operation? = null

    fun startSession(config: ExerciseConfig) {
        activeOperation = Operation.fromString(config.type)
    }

    suspend fun getNextExercise(userId: Long): Exercise {
        val userMastery = factMasteryDao.getAllFactsForUser(userId).associateBy { it.factId }
        val levels = activeOperation?.let { Curriculum.getLevelsFor(it) } ?: Curriculum.getAllLevels()
        val provider = ExerciseProvider(levels, userMastery)
        return provider.getNextExercise()
    }

    suspend fun recordAttempt(userId: Long, exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        withContext(Dispatchers.IO) {
            val wasCorrect = exercise.equation.getExpectedResult() == submittedAnswer
            val attempt = ExerciseAttempt(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                problemText = exercise.equation.question(),
                logicalOperation = exercise.getFactId().split("_")[0].let { Operation.valueOf(it) },
                correctAnswer = exercise.equation.getExpectedResult(),
                submittedAnswer = submittedAnswer,
                wasCorrect = wasCorrect,
                durationMs = durationMs
            )
            exerciseAttemptDao.insert(attempt)

            val factId = exercise.getFactId()
            val currentMastery = factMasteryDao.getFactMastery(userId, factId)
            val newStrength = if (wasCorrect) {
                (currentMastery?.strength ?: 0) + 1
            } else {
                1
            }

            val newMastery = FactMastery(
                factId = factId,
                userId = userId,
                strength = newStrength.coerceIn(1, 5),
                lastTestedTimestamp = System.currentTimeMillis()
            )
            factMasteryDao.upsert(newMastery)
        }
    }
}
