package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.DrillStrategy
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.ExerciseProvider
import com.codinglikeapirate.pocitaj.logic.ExerciseStrategy
import com.codinglikeapirate.pocitaj.logic.ReviewStrategy
import com.codinglikeapirate.pocitaj.logic.SmartPracticeStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AdaptiveExerciseSource(
    private val factMasteryDao: FactMasteryDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val userDao: UserDao,
    private val userId: Long
) : ExerciseSource {

    private var exerciseProvider: ExerciseProvider? = null
    private val EWMA_ALPHA = 0.2

    override suspend fun initialize(config: ExerciseConfig) {
        val userMastery = factMasteryDao.getAllFactsForUser(userId).first().associateBy { it.factId }
        val level = config.levelId?.let { Curriculum.getAllLevels().find { level -> level.id == it } }

        exerciseProvider = when {
            level != null && level.strategy == ExerciseStrategy.REVIEW -> ReviewStrategy(level, userMastery)
            level != null -> DrillStrategy(level, userMastery)
            else -> {
                val filteredCurriculum = Curriculum.getAllLevels().filter { it.operation == config.operation }
                SmartPracticeStrategy(filteredCurriculum, userMastery)
            }
        }
    }

    override fun getNextExercise(): Exercise? {
        return exerciseProvider?.getNextExercise()
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
            val currentStrength = currentMastery?.strength ?: 0
            val currentAvgDuration = currentMastery?.avgDurationMs ?: 0L

            val newStrength = if (wasCorrect) {
                currentStrength + 1
            } else {
                0 // Reset to 0 on incorrect
            }

            val newAvgDuration = if (wasCorrect) {
                if (currentAvgDuration == 0L) {
                    durationMs // This is the first correct answer
                } else {
                    (EWMA_ALPHA * durationMs + (1 - EWMA_ALPHA) * currentAvgDuration).toLong()
                }
            } else {
                currentAvgDuration // No change on incorrect
            }

            val newMastery = FactMastery(
                factId = factId,
                userId = userId,
                strength = newStrength.coerceAtMost(5), // Cap at 5 (mastered)
                lastTestedTimestamp = System.currentTimeMillis(),
                avgDurationMs = newAvgDuration
            )
            factMasteryDao.upsert(newMastery)
        }
    }
}