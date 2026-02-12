package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation

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

/**
 * A helper function to create an Exercise object from a fact ID string. This is used by all
 * strategies to convert the abstract fact ID into a concrete exercise.
 */
internal fun exerciseFromFactId(factId: String): Exercise {
    val parts = factId.split("_")
    val operation = Operation.valueOf(parts[0])
    val op1 = parts[1].toInt()
    val op2 = parts[2].toInt()

    val equation = when (operation) {
        Operation.ADDITION -> Addition(op1, op2)
        Operation.SUBTRACTION -> Subtraction(op1, op2)
        Operation.MULTIPLICATION -> Multiplication(op1, op2)
        Operation.DIVISION -> Division(op1, op2)
    }
    return Exercise(equation)
}

fun Level.createStrategy(
    userMastery: MutableMap<String, FactMastery>,
    activeUserId: Long,
    clock: kotlin.time.Clock = kotlin.time.Clock.System
): ExerciseProvider {
    return when {
        this is TwoDigitComputationLevel -> TwoDigitDrillStrategy(
            this, userMastery, activeUserId = activeUserId, clock = clock
        )

        this.strategy == ExerciseStrategy.REVIEW -> ReviewStrategy(
            this,
            userMastery,
            reviewStrength = 5,
            targetStrength = 6,
            activeUserId = activeUserId,
            clock = clock
        )

        else -> DrillStrategy(
            this, userMastery, activeUserId = activeUserId, clock = clock
        )
    }
}