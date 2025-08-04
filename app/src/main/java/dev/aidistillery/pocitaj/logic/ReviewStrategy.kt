package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery

/**
 * A strategy for testing the user's knowledge of a set of facts. It selects exercises randomly,
 * without repetition, and gives a slightly higher weight to exercises the student has struggled
 * with in the past.
 */
class ReviewStrategy(
    private val level: Level,
    private val userMastery: Map<String, FactMastery>
) : ExerciseProvider {
    override fun getNextExercise(): Exercise? {
        // TODO: Implement weighted random logic
        return exerciseFromFactId(level.getAllPossibleFactIds().random()) // Placeholder
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean) {
        // TODO: Implement feedback logic
    }
}
