package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.DrillStrategy
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.ExerciseProvider
import dev.aidistillery.pocitaj.logic.ExerciseStrategy
import dev.aidistillery.pocitaj.logic.ReviewStrategy
import dev.aidistillery.pocitaj.logic.SmartPracticeStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.lang.reflect.Field

class AdaptiveExerciseSource(
    private val factMasteryDao: FactMasteryDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUserManager: ActiveUserManager
) : ExerciseSource {

    private var exerciseProvider: ExerciseProvider? = null
    private val EWMA_ALPHA = 0.2

    override suspend fun initialize(config: ExerciseConfig) {
        val userMastery =
            factMasteryDao.getAllFactsForUser(activeUserManager.activeUser.id).first().associateBy { it.factId }
        val level =
            config.levelId?.let { Curriculum.getAllLevels().find { level -> level.id == it } }

        exerciseProvider = when {
            level != null && level.strategy == ExerciseStrategy.REVIEW -> ReviewStrategy(
                level,
                userMastery.toMutableMap(),
                reviewStrength = 5, // Temporary default
                targetStrength = 6,  // Temporary default
                activeUserId = activeUserManager.activeUser.id
            )

            level != null -> DrillStrategy(level, userMastery.toMutableMap(), activeUserId = activeUserManager.activeUser.id)
            else -> {
                val filteredCurriculum =
                    Curriculum.getAllLevels().filter { it.operation == config.operation }
                SmartPracticeStrategy(filteredCurriculum, userMastery.toMutableMap(), activeUserId = activeUserManager.activeUser.id)
            }
        }
    }

    override fun getNextExercise(): Exercise? {
        return exerciseProvider?.getNextExercise()
    }

    /**
     * A debug-only function to get the current working set from the DrillStrategy using reflection.
     * This is not intended for production use.
     */
    @Suppress("UNCHECKED_CAST")
    fun _getWorkingSetForDebug(): List<String> {
        if (exerciseProvider !is DrillStrategy) {
            return emptyList()
        }
        return try {
            // Get the workingSet list of fact IDs
            val wsField: Field = DrillStrategy::class.java.getDeclaredField("workingSet")
            wsField.isAccessible = true
            val workingSetIds = wsField.get(exerciseProvider) as List<String>

            // Get the userMastery map to look up strengths
            val umField: Field = DrillStrategy::class.java.getDeclaredField("userMastery")
            umField.isAccessible = true
            val userMastery = umField.get(exerciseProvider) as Map<String, FactMastery>

            // Map each fact ID to a formatted string with its strength
            workingSetIds.map { factId ->
                val strength = userMastery[factId]?.strength ?: 0
                "$factId (strength: $strength)"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun recordAttempt(exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        val wasCorrect = exercise.equation.getExpectedResult() == submittedAnswer
        val newMastery = exerciseProvider?.recordAttempt(exercise, wasCorrect)

        withContext(Dispatchers.IO) {
            val attempt = ExerciseAttempt(
                userId = activeUserManager.activeUser.id,
                timestamp = System.currentTimeMillis(),
                problemText = exercise.equation.question(),
                logicalOperation = exercise.getFactId().split("_")[0].let { Operation.valueOf(it) },
                correctAnswer = exercise.equation.getExpectedResult(),
                submittedAnswer = submittedAnswer,
                wasCorrect = wasCorrect,
                durationMs = durationMs
            )
            exerciseAttemptDao.insert(attempt)

            newMastery?.let {
                factMasteryDao.upsert(it)
            }
        }
    }
}