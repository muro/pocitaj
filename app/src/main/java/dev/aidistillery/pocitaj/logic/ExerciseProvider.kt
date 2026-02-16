package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Defines the teaching strategy for a given level. This allows the app to use different
 * learning methods (e.g., spaced repetition drills vs. random tests) for different types of content.
 */
enum class ExerciseStrategy {
    DRILL,
    REVIEW,
    SMART_PRACTICE
}

/**
 * A stateful object that manages the logic for a single exercise session. It is responsible for
 * selecting the next exercise and updating its internal state based on the user's performance.
 */
interface ExerciseProvider {
    fun getNextExercise(): Exercise?
    fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String>
}

fun Level.createStrategy(
    userMastery: MutableMap<String, FactMastery>,
    activeUserId: Long,
    clock: Clock = Clock.System,
    random: Random = Random.Default
): ExerciseProvider {
    return when {
        this.strategy == ExerciseStrategy.REVIEW -> ReviewStrategy(
            this,
            userMastery,
            activeUserId = activeUserId,
            clock = clock
        )

        else -> DrillStrategy(
            this, userMastery, activeUserId = activeUserId, clock = clock, random = random
        )
    }
}