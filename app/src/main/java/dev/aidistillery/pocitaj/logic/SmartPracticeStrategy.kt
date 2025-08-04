package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import kotlin.random.Random

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
        // TODO: Implement feedback logic
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
