package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.random.Random
import kotlin.time.Clock

private const val L3_MASTERY = 5
private const val L2_MASTERY = 3
private const val REPLACEABLE_MASTERY = L3_MASTERY - 1 // Strength 4

/**
 * Defines a learning algorithm to master a set of facts (e.g., a multiplication
 * table) using a multi-level mastery system and a dynamic "working set." This
 * strategy prioritizes weaker items and adapts to user performance, making it
 * suitable for short, focused review sessions.
 *
 * The logic is as follows:
 *
 * 1.  **Mastery Levels & Initialization:**
 * All facts begin at Level 1.
 * - **L1 (Learning):** Represents new or incorrectly answered facts that require
 * frequent, immediate review. (Strength 0-2)
 * - **L2 (Consolidating):** Represents facts that have been learned but require
 * spaced review to build long-term memory. (Strength 3-4)
 * - **L3 (Fluent):** Represents mastered facts that require only occasional
 * review to maintain fluency. (Strength 5)
 *
 * 2.  **The Dynamic Working Set (e.g., Target Size = 4):**
 * A review session operates on a small, temporary queue of the weakest facts
 * to keep the user focused.
 * - **Formation:** At the start of a session, the set is populated by pulling
 * facts in a specific priority order until the set is full:
 *   1. All L1 facts (strength 0-2).
 *   2. The most overdue L2 facts (strength 3-4, oldest timestamp first).
 *   3. New, unseen facts to fill any remaining space.
 *   4. Mastered (L3) facts, sorted by oldest timestamp, to fill any remaining space.
 * - **Maintenance:** When a fact is answered correctly and is considered "stable" (strength 4 or 5),
 * it is removed from the working set. The empty slot is then filled by the next-weakest
 * available fact from the entire level, ensuring a constant challenge.
 *
 * 3.  **Promotion Logic:**
 * a) **L1 -> L2 (Encoding):** A fact is promoted after 2 consecutive correct
 * answers within a single review session.
 * b) **L2 -> L3 (Consolidation):** A fact is promoted after being answered
 * correctly in a separate, spaced-out session.
 *
 * 4.  **Error Handling & Demotion:**
 * If a fact is answered incorrectly, it is immediately demoted to strength 0
 * and moved to the back of the working set for immediate repetition.
 */
class DrillStrategy(
    private val level: Level,
    private val userMastery: MutableMap<String, FactMastery>,
    private val workingSetSize: Int = 4,
    private val activeUserId: Long,
    private val clock: Clock = Clock.System
) : ExerciseProvider {

    internal val workingSet = mutableListOf<String>()
    private val allFactsInLevel = level.getAllPossibleFactIds()

    init {
        require(allFactsInLevel.isNotEmpty()) { "Level cannot be empty" }
        updateWorkingSet()
    }

    private fun getMastery(factId: String): FactMastery {
        return userMastery[factId] ?: FactMastery(factId, activeUserId, "", 3, 0)
    }

    private fun updateWorkingSet() {
        // Separate all facts into seen (in userMastery) and unseen.
        val (seenFactIds, unseenFactIds) = allFactsInLevel.partition { userMastery.containsKey(it) }

        // From the seen facts, get the ones that are not fully mastered.
        val seenMastery = seenFactIds.map { getMastery(it) }
        val (masteredFacts, nonMasteredSeenFacts) = seenMastery.partition { it.strength >= L3_MASTERY }

        // Separate the non-mastered facts into L1 (learning) and L2 (consolidating).
        val (l1Facts, l2Facts) = nonMasteredSeenFacts.partition { it.strength < L2_MASTERY }

        // Sort the L2 facts to prioritize the ones that were tested longest ago.
        val sortedL2Facts = l2Facts.sortedBy { it.lastTestedTimestamp }

        // Build the final list of candidates in the correct priority order.
        val potentialFactIds = (
                l1Facts.map { it.factId } +
                        sortedL2Facts.map { it.factId } +
                        unseenFactIds +
                        masteredFacts.shuffled().map { it.factId }
                ).distinct()

        workingSet.clear()
        workingSet.addAll(potentialFactIds.take(workingSetSize))
    }

    override fun getNextExercise(): Exercise? {
        if (workingSet.isEmpty()) return null

        var selectedIndex = workingSet.lastIndex
        for (i in 0 until workingSet.lastIndex) {
            if (Random.nextDouble() < 0.5) {
                selectedIndex = i
                break
            }
        }

        val factId = workingSet.removeAt(selectedIndex)
        workingSet.add(factId)
        return exerciseFromFactId(factId)
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean): Pair<FactMastery?, String> {
        val factId = exercise.getFactId()
        val mastery = getMastery(factId)
        val now = clock.now().toEpochMilliseconds()
        val duration = exercise.timeTakenMillis?.toLong() ?: 0L

        val newAvgDuration = if (mastery.avgDurationMs > 0) {
            (mastery.avgDurationMs * 0.8 + duration * 0.2).toLong()
        } else {
            duration
        }

        val newStrength = if (wasCorrect) {
            val speedBadge = exercise.speedBadge
            when (val currentStrength = mastery.strength) {
                0, 1 -> currentStrength + 1 // Always advance for accuracy at low levels
                2 -> if (speedBadge >= SpeedBadge.BRONZE) 3 else 2
                3 -> if (speedBadge >= SpeedBadge.SILVER) 4 else 3
                4 -> if (speedBadge == SpeedBadge.GOLD) 5 else 4
                else -> currentStrength // Stays at 5 if already mastered
            }
        } else {
            1 // Reset to 1 on failure
        }

        val newMastery = mastery.copy(
            strength = newStrength.coerceAtMost(L3_MASTERY),
            lastTestedTimestamp = now,
            avgDurationMs = newAvgDuration
        )
        userMastery[factId] = newMastery

        if (newMastery.strength >= REPLACEABLE_MASTERY) {
            workingSet.remove(factId)
            addNextWeakestFact()
        }
        return newMastery to level.id
    }

    private fun addNextWeakestFact() {
        if (workingSet.size < workingSetSize) {
            val candidates = allFactsInLevel
                .filter { it !in workingSet }
                .sortedWith(
                    compareBy(
                        { getMastery(it).strength },
                        { getMastery(it).lastTestedTimestamp })
                )
                .take(workingSetSize)

            if (candidates.isNotEmpty()) {
                var selectedIndex = candidates.lastIndex
                for (i in 0 until candidates.lastIndex) {
                    if (Random.nextDouble() < 0.5) {
                        selectedIndex = i
                        break
                    }
                }
                workingSet.add(candidates[selectedIndex])
            }
        }
    }
}
