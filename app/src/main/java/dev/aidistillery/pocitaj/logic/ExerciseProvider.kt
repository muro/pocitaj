package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import kotlin.random.Random

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
    fun recordAttempt(exercise: Exercise, wasCorrect: Boolean)
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

/**
 * A strategy for practicing a large, mixed set of exercises. It uses a combination of heuristics
 * to balance the introduction of new material with the review of mastered concepts.
 */
class SmartPracticeStrategy(
    private val curriculum: List<Level>,
    private val userMastery: Map<String, FactMastery>,
    private val random: Random = Random.Default
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
        val isLearningExercise = masteredLevels.isEmpty() || random.nextFloat() < LEARNING_EXERCISE_PROBABILITY

        val levelToPractice = if (isLearningExercise) {
            currentLevel
        } else {
            masteredLevels.random(random)
        }

        val factId = findWeakestFactIn(levelToPractice)
        return exerciseFromFactId(factId)
    }

    override fun recordAttempt(exercise: Exercise, wasCorrect: Boolean) {
        // TODO: Implement feedback logic
    }

    private fun findWeakestFactIn(level: Level): String {
        val allFactsInLevel = level.getAllPossibleFactIds()
        val unmasteredFacts = allFactsInLevel.filter { (userMastery[it]?.strength ?: 0) < MASTERY_STRENGTH }

        val workingSet = if (unmasteredFacts.size < WORKING_SET_SIZE) {
            val newFactsNeeded = WORKING_SET_SIZE - unmasteredFacts.size
            val unseenFacts = allFactsInLevel.filter { !userMastery.containsKey(it) }
            unmasteredFacts + unseenFacts.shuffled(random).take(newFactsNeeded)
        } else {
            unmasteredFacts.sortedWith(compareBy(
                { userMastery[it]?.strength ?: 0 },
                { userMastery[it]?.lastTestedTimestamp ?: 0L },
                { it.split("_")[1].toInt() },
                { it.split("_")[2].toInt() }
            )).take(WORKING_SET_SIZE)
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
    private fun isLevelMastered(level: Level): Boolean {
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
    private fun isLevelUnlocked(level: Level): Boolean {
        return level.prerequisites.all { prerequisiteId ->
            val prerequisiteLevel = curriculum.find { it.id == prerequisiteId }
            prerequisiteLevel?.let { isLevelMastered(it) } ?: false
        }
    }
}
