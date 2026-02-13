package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.time.Clock

/**
 * Encapsulates the core rules of the Spaced Repetition System (SRS).
 * Determines how a fact's mastery strength changes based on user performance.
 */
object SpacedRepetitionSystem {

    private const val L3_MASTERY = 5

    /**
     * Updates the mastery state of a fact based on the result of an exercise.
     *
     * @param currentMastery The current mastery state of the fact.
     * @param wasCorrect Whether the user answered correctly.
     * @param durationMs The time taken to answer in milliseconds.
     * @param speedBadge The speed badge earned (GOLD, SILVER, BRONZE, or NONE).
     * @param clock The clock to use for timestamping (defaults to System clock).
     * @return A new [FactMastery] instance with updated stats.
     */
    fun updateMastery(
        currentMastery: FactMastery,
        wasCorrect: Boolean,
        durationMs: Long,
        speedBadge: SpeedBadge,
        clock: Clock = Clock.System
    ): FactMastery {
        val now = clock.now().toEpochMilliseconds()

        // 1. Update Average Duration (Weighted Moving Average)
        val newAvgDuration = if (currentMastery.avgDurationMs > 0) {
            (currentMastery.avgDurationMs * 0.8 + durationMs * 0.2).toLong()
        } else {
            durationMs
        }

        // 2. Update Strength
        val newStrength = if (wasCorrect) {
            when (val currentStrength = currentMastery.strength) {
                0, 1 -> currentStrength + 1 // Always advance for accuracy at low levels
                2 -> if (speedBadge >= SpeedBadge.BRONZE) 3 else 2
                3 -> if (speedBadge >= SpeedBadge.SILVER) 4 else 3
                4 -> if (speedBadge == SpeedBadge.GOLD) 5 else 4
                else -> currentStrength // Stays at 5 if already mastered
            }
        } else {
            1 // Reset to 1 on failure
        }

        return currentMastery.copy(
            strength = newStrength.coerceAtMost(L3_MASTERY),
            lastTestedTimestamp = now,
            avgDurationMs = newAvgDuration
        )
    }
}
