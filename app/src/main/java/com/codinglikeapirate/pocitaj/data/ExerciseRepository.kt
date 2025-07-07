package com.codinglikeapirate.pocitaj.data

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

    suspend fun getNextExercise(userId: Long): Exercise {
        val userMastery = factMasteryDao.getAllFactsForUser(userId).associateBy { it.factId }
        val provider = ExerciseProvider(Curriculum.getAllLevels(), userMastery)
        return provider.getNextExercise()
    }

    suspend fun recordAttempt(userId: Long, exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        withContext(Dispatchers.IO) {
            val wasCorrect = exercise.result == submittedAnswer
            val attempt = ExerciseAttempt(
                userId = userId,
                timestamp = System.currentTimeMillis(),
                problemText = "${exercise.operand1} ${exercise.operation.name} ${exercise.operand2}",
                logicalOperation = exercise.operation,
                correctAnswer = exercise.result,
                submittedAnswer = submittedAnswer,
                wasCorrect = wasCorrect,
                durationMs = durationMs
            )
            exerciseAttemptDao.insert(attempt)

            val factId = "${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}"
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
