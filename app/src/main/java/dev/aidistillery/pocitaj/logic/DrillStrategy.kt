package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery

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
 * facts in priority order: first all L1 facts, then the most overdue L2 facts.
 * - **Maintenance:** The set size is generally maintained via a "remove and replace"
 * mechanism. When a fact is promoted (L1->L2) or an L2 fact is successfully
 * reviewed, it is removed, and the next-weakest fact from the overall pool
 * is immediately added.
 *
 * 3.  **Promotion Logic:**
 * a) **L1 -> L2 (Encoding):** A fact is promoted after 2-3 consecutive correct
 * answers within a single review session. This confirms initial learning.
 * b) **L2 -> L3 (Consolidation):** A fact is promoted after being answered
 * correctly in at least two separate, spaced-out sessions (e.g., on
 * different days). This proves long-term recall.
 *
 * 4.  **Error Handling & Demotion:**
 * If a fact at L2 or L3 is answered incorrectly, it is immediately demoted to L1
 * and **injected into the current working set.** This provides the fastest
 * possible feedback loop, and the working set may temporarily exceed its
 * target size to accommodate this high-priority item.
*/
class DrillStrategy(
    private val level: Level,
    private val userMastery: MutableMap<String, FactMastery>,
    private val workingSetSize: Int = 4
) : ExerciseProvider {

    private val workingSet = mutableListOf<String>()
    private val allFactsInLevel = level.getAllPossibleFactIds()
    private val consecutiveCorrectAnswers = mutableMapOf<String, Int>()

    companion object {
        private const val CONSECUTIVE_ANSWERS_FOR_PROMOTION = 2
        // To ensure that L2 -> L3 promotion happens in a separate session, we require
        // a minimum time gap between reviews. 5 minutes is a practical proxy for a
        // "different session" in a mobile app context.
        private const val MIN_SESSION_SPACING_MS = 5 * 60 * 1000
    }

    init {
        updateWorkingSet()
    }

    private fun getMastery(factId: String): FactMastery {
        return userMastery[factId] ?: FactMastery(factId, 1, 0, 0)
    }

    private fun updateWorkingSet() {
        val nonMasteredFacts = allFactsInLevel.filter { getMastery(it).strength < 5 }

        val (l1Facts, l2Facts) = nonMasteredFacts.partition { getMastery(it).strength < 3 }

        val sortedL2 = l2Facts.sortedBy { getMastery(it).lastTestedTimestamp }

        val potentialFacts = (l1Facts + sortedL2).distinct()

        workingSet.clear()
        workingSet.addAll(potentialFacts.take(workingSetSize))
    }

    override fun getNextExercise(): Exercise? {
        if (workingSet.isEmpty()) {
            updateWorkingSet()
            if (workingSet.isEmpty()) return null
        }
        return exerciseFromFactId(workingSet.first())
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean) {
        val factId = exercise.getFactId()
        val mastery = getMastery(factId)

        if (wasCorrect) {
            consecutiveCorrectAnswers[factId] = (consecutiveCorrectAnswers[factId] ?: 0) + 1
            var newStrength = mastery.strength

            // Promotion L1 -> L2
            if (mastery.strength < 3 && (consecutiveCorrectAnswers[factId] ?: 0) >= CONSECUTIVE_ANSWERS_FOR_PROMOTION) {
                newStrength = 3
            }
            // Promotion L2 -> L3
            else if (mastery.strength in 3..4) {
                val lastTested = mastery.lastTestedTimestamp
                val now = System.currentTimeMillis()
                // Check if it was tested in a different session
                if (now - lastTested > MIN_SESSION_SPACING_MS) {
                    newStrength = 5
                } else {
                    newStrength = mastery.strength + 1
                }
            } else {
                newStrength = mastery.strength + 1
            }

            userMastery[factId] = mastery.copy(
                strength = newStrength.coerceAtMost(5),
                lastTestedTimestamp = System.currentTimeMillis()
            )

            workingSet.remove(factId)
            updateWorkingSet()

        } else { // Incorrect Answer
            consecutiveCorrectAnswers[factId] = 0
            userMastery[factId] = mastery.copy(strength = 0, lastTestedTimestamp = System.currentTimeMillis())

            // Demotion and Injection
            if (!workingSet.contains(factId)) {
                workingSet.add(0, factId) // Inject to the front
            } else {
                // Move to the back of the queue for immediate repetition
                workingSet.remove(factId)
                workingSet.add(factId)
            }
        }
    }
}
