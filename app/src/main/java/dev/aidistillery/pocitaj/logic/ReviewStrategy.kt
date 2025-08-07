package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.time.Clock


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
 *     - A score > 1.0 means a fact is "overdue." The threshold is configurable via `URGENCY_THRESHOLD`.
 *     - The ideal interval grows with the fact's strength (e.g., Strength 1: 4 hours, Strength 5: 2 weeks).
 *     - New, unseen facts are given a default, constant urgency to ensure they are introduced into the mix.
 *
 * 3.  **Exercise Selection:**
 *     - A temporary candidate pool is created containing only the "seen" facts that are due for review (i.e., urgency > threshold).
 *     - **If and only if** this pool of overdue facts is empty, it is replaced by a new pool consisting of all unseen facts.
 *     - The final exercise is chosen via a **weighted random selection** from the active candidate pool.
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
    private val userMastery: MutableMap<String, FactMastery>,
    private val clock: Clock = Clock.System
) : ExerciseProvider {

    companion object {
        private const val MAX_STRENGTH = 5

        // Defines the ideal time gap in milliseconds before a fact should be reviewed again.
        private val idealIntervals = mapOf(
            0 to 10 * 1000L,                      // 10 seconds
            1 to 8 * 60 * 60 * 1000L,             // 8 hours
            2 to 24 * 60 * 60 * 1000L,            // 1 day
            3 to 3 * 24 * 60 * 60 * 1000L,        // 3 days
            4 to 7 * 24 * 60 * 60 * 1000L,        // 1 week
            5 to 2 * 7 * 24 * 60 * 60 * 1000L     // 2 weeks
        )
        // A fact is considered "due" for review when its urgency score is above this threshold.
        private const val URGENCY_THRESHOLD = 0.75
        // The default urgency for a fact that has never been seen before.
        private const val UNSEEN_FACT_URGENCY = 1.0
    }

    override fun getNextExercise(): Exercise? {
        val allFactsInLevel = level.getAllPossibleFactIds()
        if (allFactsInLevel.isEmpty()) {
            return null
        }

        val now = clock.now().toEpochMilliseconds()
        val (seenFactIds, unseenFactIds) = allFactsInLevel.partition { userMastery.containsKey(it) }

        // Calculate urgency scores for all facts the user has seen before.
        val seenUrgencyScores = seenFactIds.associateWith { factId ->
            val mastery = userMastery[factId]!! // Known to exist due to partition
            val interval = idealIntervals[mastery.strength] ?: idealIntervals.values.last()
            val elapsed = now - mastery.lastTestedTimestamp
            elapsed.toDouble() / interval
        }

        // The primary candidate pool consists of facts that are due for review.
        var candidates = seenUrgencyScores.filter { it.value > URGENCY_THRESHOLD }

        // If no facts are due for review, the candidate pool becomes the unseen facts.
        if (candidates.isEmpty()) {
            if (unseenFactIds.isNotEmpty()) {
                candidates = unseenFactIds.associateWith { UNSEEN_FACT_URGENCY }
            } else if (seenFactIds.isNotEmpty()) {
                // If there are no unseen facts left, just review the least recently tested fact.
                val leastRecentFactId = seenFactIds.minByOrNull { userMastery[it]!!.lastTestedTimestamp }
                return leastRecentFactId?.let { exerciseFromFactId(it) }
            } else {
                return null // Nothing to do.
            }
        }

        // Perform weighted random selection from the chosen candidate pool.
        val totalUrgency = candidates.values.sum()
        var randomPoint = Math.random() * totalUrgency

        for ((factId, urgency) in candidates) {
            if (randomPoint < urgency) {
                return exerciseFromFactId(factId)
            }
            randomPoint -= urgency
        }

        // Fallback in case of rounding errors.
        return candidates.keys.firstOrNull()?.let { exerciseFromFactId(it) }
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean) {
        val factId = exercise.getFactId()
        val now = clock.now().toEpochMilliseconds()
        val mastery = userMastery[factId] ?: FactMastery(factId, 1, 0, 0)

        val newStrength = if (wasCorrect) {
            (mastery.strength + 1).coerceAtMost(MAX_STRENGTH)
        } else {
            1 // Reset strength on failure
        }

        userMastery[factId] = mastery.copy(
            strength = newStrength,
            lastTestedTimestamp = now
        )
    }
}
