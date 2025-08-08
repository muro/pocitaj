package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.random.Random
import kotlin.time.Clock

/**
 * A sophisticated strategy for long-term, mixed-subject practice sessions. This strategy dynamically
 * balances the introduction of new material with the spaced repetition of previously mastered concepts,
 * making it ideal for a "Practice" mode where the user wants to work on a variety of problems
 * without being confined to a single level.
 *
 * ### Core Philosophy
 *
 * The strategy operates on a simple but powerful heuristic: **80% of the time, focus on new
 * material; 20% of the time, review old material.** This ensures steady progress through the
 * curriculum while actively combating the forgetting curve for concepts the user has already "mastered."
 *
 * ### How it Works
 *
 * 1.  **Identify the Learning Frontier:**
 *     - The strategy first determines the user's `currentLevel`. This is the first level in the
 *       curriculum that is not yet fully mastered and for which all prerequisites have been met.
 *     - It also identifies all `masteredLevels` that come before the `currentLevel`.
 *
 * 2.  **The 80/20 Rule:**
 *     - On every call to `getNextExercise()`, a probabilistic choice is made:
 *         - With an 80% probability (`LEARNING_EXERCISE_PROBABILITY`), it chooses to practice a
 *           fact from the `currentLevel`.
 *         - With a 20% probability, it randomly selects one of the `masteredLevels` for a
 *           quick review.
 *     - If the user has no mastered levels yet, it will always focus on the `currentLevel`.
 *
 * 3.  **Find the Weakest Fact:**
 *     - Once a level has been selected for practice (either the current one or a review one), the
 *       strategy identifies the "weakest" facts within that level.
 *     - A `workingSet` of the 5 weakest facts is created. Weakness is determined by:
 *         1.  Lowest mastery strength.
 *         2.  Oldest `lastTestedTimestamp` (for tie-breaking).
 *     - If there are fewer than 5 unmastered facts, the working set is padded with new, unseen facts.
 *
 * 4.  **Random Selection from the Weakest:**
 *     - To prevent the practice from becoming too predictable, the final exercise is chosen by
 *       **randomly selecting a fact from the `workingSet`**.
 *     - If the entire level is already mastered (e.g., during a review session), a random fact
 *       from that level is chosen.
 *
 * ### Feedback Mechanism (Future Work)
 *
 * The `recordAttempt` function is currently a placeholder. A complete implementation should:
 * - Demote the strength of a fact if the user answers incorrectly, especially during a review.
 * - Potentially increase the strength if answered correctly, though this might be less aggressive
 *   than in `DrillStrategy` to avoid rapidly re-mastering a concept that should be in long-term
 *   review.
 */
class SmartPracticeStrategy(
    private val curriculum: List<Level>,
    private val userMastery: MutableMap<String, FactMastery>,
    private val random: Random = Random.Default,
    private val clock: Clock = Clock.System
) : ExerciseProvider {
    companion object {
        private const val MASTERY_STRENGTH = 5
        private const val LEARNING_EXERCISE_PROBABILITY = 0.8f
        private const val WORKING_SET_SIZE = 5
    }

    override fun getNextExercise(): Exercise {
        val currentLevel = findCurrentLevel()
        val masteredLevels = getMasteredLevels(currentLevel)

        // This is the core of the smart practice logic. It ensures that the user spends most of their
        // time on new material, but also periodically reviews older concepts to prevent forgetting.
        val isLearningExercise =
            masteredLevels.isEmpty() || random.nextFloat() < LEARNING_EXERCISE_PROBABILITY

        val levelToPractice = if (isLearningExercise) {
            currentLevel
        } else {
            masteredLevels.random(random)
        }

        val factId = findWeakestFactIn(levelToPractice)
        return exerciseFromFactId(factId)
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean) {
        val factId = exercise.getFactId()
        val now = clock.now().toEpochMilliseconds()
        val mastery = userMastery[factId] ?: FactMastery(factId, 1, 0, 0)

        val newStrength = if (wasCorrect) {
            (mastery.strength + 1).coerceAtMost(MASTERY_STRENGTH)
        } else {
            1 // Reset strength on failure
        }

        userMastery[factId] = mastery.copy(
            strength = newStrength,
            lastTestedTimestamp = now
        )
    }

    private fun findWeakestFactIn(level: Level): String {
        val allFactsInLevel = level.getAllPossibleFactIds()
        val unmasteredFacts =
            allFactsInLevel.filter { (userMastery[it]?.strength ?: 0) < MASTERY_STRENGTH }

        val workingSet = if (unmasteredFacts.size < WORKING_SET_SIZE) {
            val newFactsNeeded = WORKING_SET_SIZE - unmasteredFacts.size
            val unseenFacts = allFactsInLevel.filter { !userMastery.containsKey(it) }
            unmasteredFacts + unseenFacts.shuffled(random).take(newFactsNeeded)
        } else {
            unmasteredFacts.sortedWith(
                compareBy(
                    { userMastery[it]?.strength ?: 0 },
                    { userMastery[it]?.lastTestedTimestamp ?: 0L },
                    { it.split("_")[1].toInt() },
                    { it.split("_")[2].toInt() }
                )
            ).take(WORKING_SET_SIZE)
        }

        return if (workingSet.isNotEmpty()) {
            workingSet.random(random)
        } else {
            allFactsInLevel.random(random)
        }
    }

    /**
     * Finds the user's current level. This is the first level in the curriculum
     * that is not yet fully mastered.
     */
    private fun findCurrentLevel(): Level {
        return curriculum.firstOrNull { isLevelUnlocked(it) && !isLevelMastered(it) }
            ?: curriculum.last()
    }

    /**
     * Gets a list of all levels that come before the current level.
     */
    private fun getMasteredLevels(currentLevel: Level): List<Level> {
        val currentIndex = curriculum.indexOf(currentLevel)
        return if (currentIndex > 0) {
            curriculum.subList(0, currentIndex).filter { isLevelMastered(it) }
        } else {
            emptyList()
        }
    }

    /**
     * Checks if a given level is fully mastered. A level is considered mastered if
     * every possible fact within it has reached the MASTERY_STRENGTH.
     */
    internal fun isLevelMastered(level: Level): Boolean {
        val allFactsInLevel = level.getAllPossibleFactIds()
        if (allFactsInLevel.isEmpty()) return true

        return allFactsInLevel.all { factId ->
            (userMastery[factId]?.strength ?: 0) >= MASTERY_STRENGTH
        }
    }

    /**
     * Checks if a given level is unlocked. A level is unlocked if all of its
     * prerequisites have been mastered.
     */
    internal fun isLevelUnlocked(level: Level): Boolean {
        return level.prerequisites.all { prerequisiteId ->
            val prerequisiteLevel = curriculum.find { it.id == prerequisiteId }
            prerequisiteLevel?.let { isLevelMastered(it) } ?: false
        }
    }
}
