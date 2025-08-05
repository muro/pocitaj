package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery

/**
 * Implements a spaced repetition strategy for long-term retention of learned facts.
 * This strategy is stateless and calculates the optimal exercise on-the-fly for each call,
 * making it ideal for review sessions spaced out over time.
 *
 * The core logic is based on calculating a "review urgency" for every fact in the level.
 * This ensures that facts most at risk of being forgotten are prioritized, while still
 * introducing new material organically.
 *
 * ### How it Works:
 *
 * 1.  **Stateless Calculation:**
 *     - Unlike `DrillStrategy`, this strategy does not maintain a persistent "working set."
 *     - Every time `getNextExercise()` is called, it re-evaluates the entire set of facts
 *       in the level based on the user's latest mastery data.
 *
 * 2.  **Review Urgency Score:**
 *     - For each fact, a score is calculated: `Urgency = (Time Since Last Review) / (Ideal Interval for its Strength)`.
 *     - A score > 1.0 means a fact is "overdue."
 *     - The ideal interval grows with the fact's strength (e.g., Strength 1: 4 hours, Strength 5: 2 weeks).
 *     - New, unseen facts are given a default, constant urgency to ensure they are introduced into the mix.
 *
 * 3.  **Exercise Selection:**
 *     - A temporary candidate pool is created containing facts with the highest urgency scores.
 *     - If the pool of overdue facts is small, it's supplemented with new, unseen facts.
 *     - The final exercise is chosen via a **weighted random selection** from this pool, where
 *       higher urgency scores increase the probability of being selected.
 *     - This entire candidate pool is discarded immediately after one exercise is chosen.
 *
 * 4.  **Mastery Adjustment:**
 *     - A correct answer increases a fact's strength and updates its last-tested timestamp,
 *       pushing its next review further into the future.
 *     - An incorrect answer significantly reduces a fact's strength, making it a high-priority
 *       candidate for the next call to `getNextExercise()`.
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
