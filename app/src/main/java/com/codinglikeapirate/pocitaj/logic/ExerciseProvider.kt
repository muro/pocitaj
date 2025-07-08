package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.Operation
import kotlin.random.Random

/**
 * The main decision-making engine for selecting exercises.
 *
 * This class is stateless; it receives the user's current progress and makes a
 * decision based on that data.
 *
 * @param curriculum The complete, ordered list of all defined Level objects.
 * @param userMastery A map representing the user's current knowledge. The key is a
 *                    unique factId (e.g., "ADDITION_2_3"), and the value is the
 *                    FactMastery object, which contains the strength and last-tested
 *                    timestamp for that fact. This map is the memory of the system.
 * @param random The random number generator to use for probabilistic selections.
 */
class ExerciseProvider(
    private val curriculum: List<Level>,
    private val userMastery: Map<String, FactMastery>,
    private val random: Random = Random.Default
) {
    companion object {
        private const val MASTERY_STRENGTH = 5
        private const val LEARNING_EXERCISE_PROBABILITY = 0.8f
        private const val WORKING_SET_SIZE = 5
    }

    /**
     * Intelligently selects and returns the next best exercise for the user.
     */
    fun getNextExercise(): Exercise {
        val currentLevel = findCurrentLevel()
        val masteredLevels = getMasteredLevels(currentLevel)

        // Decide whether to practice a new concept or review an old one.
        val isLearningExercise = masteredLevels.isEmpty() || random.nextFloat() < LEARNING_EXERCISE_PROBABILITY

        val levelToPractice = if (isLearningExercise) {
            // Focus on the current, unmastered level.
            currentLevel
        } else {
            // Pick a random mastered level to review and prevent forgetting.
            masteredLevels.random(random)
        }

        val factId = findWeakestFactIn(levelToPractice)
        // TODO: consider refactoring this in the future
        val parts = factId.split("_")
        val operation = Operation.valueOf(parts[0])
        val op1 = parts[1].toInt()
        val op2 = parts[2].toInt()

        val equation = when (operation) {
            Operation.ADDITION -> Addition(op1, op2)
            Operation.SUBTRACTION -> Subtraction(op1, op2)
            Operation.MULTIPLICATION -> Multiplication(op1, op2)
            Operation.DIVISION -> throw NotImplementedError("Division not yet implemented")
        }
        return Exercise(equation)
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
                { userMastery[it]?.lastTestedTimestamp ?: 0 },
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
}
