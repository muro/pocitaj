package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.ExerciseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AdaptiveExerciseSource internal constructor(
    private val factMasteryDao: FactMasteryDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val userDao: UserDao,
    private val userId: Long,
    private var exerciseProvider: ExerciseProvider
) : ExerciseSource {

    private var currentOperation: Operation? = null

    override fun initialize(config: ExerciseConfig) {
        currentOperation = config.operation
        // Re-create the exercise provider with a filtered curriculum
        val filteredCurriculum = if (config.levelId != null) {
            Curriculum.getAllLevels().filter { it.id == config.levelId }
        } else {
            Curriculum.getAllLevels().filter { it.operation == currentOperation }
        }
        val userMastery = runBlocking {
            factMasteryDao.getAllFactsForUser(userId).first().associateBy { it.factId }
        }
        exerciseProvider = ExerciseProvider(filteredCurriculum, userMastery)
    }

    companion object {
        suspend fun create(
            factMasteryDao: FactMasteryDao,
            exerciseAttemptDao: ExerciseAttemptDao,
            userDao: UserDao,
            userId: Long = 1L
        ): AdaptiveExerciseSource {
            val userMastery = withContext(Dispatchers.IO) {
                factMasteryDao.getAllFactsForUser(userId).first().associateBy { it.factId }
            }
            val exerciseProvider = ExerciseProvider(Curriculum.getAllLevels(), userMastery)
            return AdaptiveExerciseSource(factMasteryDao, exerciseAttemptDao, userDao, userId, exerciseProvider)
        }
    }

    override suspend fun getNextExercise(): Exercise? {
        return withContext(Dispatchers.IO) {
            exerciseProvider.getNextExercise()
        }
    }

    override suspend fun recordAttempt(exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
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