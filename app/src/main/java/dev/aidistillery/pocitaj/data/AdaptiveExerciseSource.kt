package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.DrillStrategy
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.logic.ExerciseProvider
import dev.aidistillery.pocitaj.logic.SmartPracticeStrategy
import dev.aidistillery.pocitaj.logic.createStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.lang.reflect.Field

class AdaptiveExerciseSource(
    private val factMasteryDao: FactMasteryDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUserManager: ActiveUserManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ExerciseSource {

    private var exerciseProvider: ExerciseProvider? = null
    override var currentLevelId: String? = null
        private set

    private var initialStars: Int? = null

    override suspend fun initialize(config: ExerciseConfig) {
        currentLevelId = config.levelId
        val userMastery =
            factMasteryDao.getAllFactsForUser(activeUserManager.activeUser.id).first()
                .associateBy { it.factId }
        val level =
            config.levelId?.let { Curriculum.getAllLevels().find { level -> level.id == it } }
        initialStars = level?.let {
            val levelMasteryMap =
                userMastery.filterKeys { factId -> factId in it.getAllPossibleFactIds() }
            it.calculateStars(levelMasteryMap)
        }

        exerciseProvider = if (level != null) {
            level.createStrategy(
                userMastery.toMutableMap(),
                activeUserManager.activeUser.id
            )
        } else {
            val filteredCurriculum =
                Curriculum.getAllLevels().filter { it.operation == config.operation }
            SmartPracticeStrategy(
                filteredCurriculum,
                userMastery.toMutableMap(),
                activeUserId = activeUserManager.activeUser.id
            )
        }
    }

    override fun getNextExercise(): Exercise? {
        return exerciseProvider?.getNextExercise()
    }

    override suspend fun recordAttempt(exercise: Exercise, submittedAnswer: Int, durationMs: Long) {
        val wasCorrect = exercise.equation.getExpectedResult() == submittedAnswer
        val (newMastery, level) = exerciseProvider?.recordAttempt(exercise, wasCorrect) ?: return

        withContext(ioDispatcher) {
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

            newMastery?.let {
                factMasteryDao.upsert(it, level)
            }
        }
    }

    override suspend fun getSessionResult(history: List<Exercise>): SessionResult {
        val finalStars = currentLevelId?.let { levelId ->
            val level = Curriculum.getAllLevels().find { it.id == levelId } ?: return@let 0
            val masteries =
                factMasteryDao.getAllFactsForUser(activeUserManager.activeUser.id).first()
                    .associateBy { it.factId }
            val levelMasteryMap = masteries.filterKeys { it in level.getAllPossibleFactIds() }
            level.calculateStars(levelMasteryMap)
        } ?: 0

        val results = history.map { exercise ->
            dev.aidistillery.pocitaj.ui.exercise.ResultDescription(
                exercise.equationString(),
                dev.aidistillery.pocitaj.ui.exercise.ResultStatus.fromBooleanPair(
                    exercise.solved,
                    exercise.correct()
                ),
                exercise.timeTakenMillis ?: 0,
                exercise.speedBadge
            )
        }

        return SessionResult(
            results,
            StarProgress(initialStars ?: 0, finalStars)
        )
    }

    /**
     * A debug-only function to get the current working set from the DrillStrategy using reflection.
     * This is not intended for production use.
     */
    @Suppress("UNCHECKED_CAST")
    fun getWorkingSetForDebug(): List<String> {
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
}