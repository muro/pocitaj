package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.FactMastery
import kotlin.random.Random

/**
 * The main decision-making engine for selecting exercises.
 *
 * This class is stateless; it receives the user's current progress and makes a
 * decision based on that data.
 *
 * @param curriculum The complete, ordered list of all defined Level objects.
 * @param userMastery A map representing the user's current mastery of facts.
 *                    The key is the factId (e.g., "ADD_2_3") and the value is the FactMastery object.
 */
class ExerciseProvider(
    private val curriculum: List<Level>,
    private val userMastery: Map<String, FactMastery>,
    private val random: Random = Random.Default
) {
    companion object {
        private const val MASTERY_STRENGTH = 5
        private const val LEARNING_EXERCISE_PROBABILITY = 0.8f
    }

    /**
     * Intelligently selects and returns the next best exercise for the user.
     */
    fun getNextExercise(): Exercise {
        var currentLevel = findCurrentLevel()
        val masteredLevels = getMasteredLevels(currentLevel)

        // Decide whether to practice a new concept or review an old one.
        val isLearningExercise = masteredLevels.isEmpty() || random.nextFloat() < LEARNING_EXERCISE_PROBABILITY

        var levelToPractice = if (isLearningExercise) {
            // Focus on the current, unmastered level.
            currentLevel
        } else {
            // Pick a random mastered level to review and prevent forgetting.
            masteredLevels.random(random)
        }

        // Find the weakest fact within that chosen level.
        var weakestFactId = findWeakestFactIn(levelToPractice)
        var strength = userMastery[weakestFactId]?.strength ?: 0

        // If the weakest fact is already mastered, it means the level is complete.
        // Move to the next level.
        if (strength >= MASTERY_STRENGTH) {
            currentLevel = findCurrentLevel()
            levelToPractice = currentLevel
            weakestFactId = findWeakestFactIn(levelToPractice)
        }


        // Generate the exercise from the chosen fact ID.
        return Exercise.fromFactId(weakestFactId)
    }

    /**
     * Finds the user's current level. This is the first level in the curriculum
     * that is not yet fully mastered.
     */
    fun findCurrentLevel(): Level {
        return curriculum.firstOrNull { !isLevelMastered(it) }
            ?: curriculum.last() // If all levels are mastered, default to the last one for practice.
    }

    /**
     * Gets a list of all levels that come before the current level.
     */
    private fun getMasteredLevels(currentLevel: Level): List<Level> {
        val currentIndex = curriculum.indexOf(currentLevel)
        return if (currentIndex > 0) curriculum.subList(0, currentIndex) else emptyList()
    }

    /**
     * Checks if a given level is fully mastered. A level is considered mastered if
     * every possible fact within it has reached the MASTERY_STRENGTH.
     */
    private fun isLevelMastered(level: Level): Boolean {
        val allFactsInLevel = level.getAllPossibleFactIds()
        if (allFactsInLevel.isEmpty()) return true // An empty level is considered mastered.

        return allFactsInLevel.all { factId ->
            userMastery[factId]?.strength ?: 0 >= MASTERY_STRENGTH
        }
    }

    /**
     * Finds the weakest fact within a given level.
     * It finds the fact with the lowest strength. As a tie-breaker, it chooses the
     * fact that was tested the longest time ago.
     *
     * @param level The level to search within.
     * @return The factId string of the weakest fact.
     */
    private fun findWeakestFactIn(level: Level): String {
        val allFactsInLevel = level.getAllPossibleFactIds()
        val knownFacts = allFactsInLevel.filter { userMastery.containsKey(it) }

        if (knownFacts.isNotEmpty()) {
            return knownFacts.minWithOrNull(compareBy(
                { userMastery[it]?.strength ?: 0 },
                { userMastery[it]?.lastTestedTimestamp ?: 0 },
                { it.split("_")[1].toInt() },
                { it.split("_")[2].toInt() }
            ))!!
        }

        // If no facts are known, return a random one.
        return allFactsInLevel.random(random)
    }
}
