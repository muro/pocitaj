package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation

/**
 * Represents a single, teachable level in the curriculum.
 * Its primary job is to generate random exercises that conform to its rules.
 */
interface Level {
    val id: String
    val operation: Operation
    val prerequisites: Set<String>
    val strategy: ExerciseStrategy
    fun generateExercise(): Exercise
    fun getAllPossibleFactIds(): List<String>

    fun createExercise(factId: String): Exercise = Exercise(Equation.parse(factId)!!)

    /**
     * Determines which fact IDs are impacted by solving the given exercise.
     *
     * This allows a single exercise to update mastery for multiple underlying skills.
     * For example, `19 + 19` (Composite Fact) might update mastery for `9 + 9` (Ones) and `10 + 10` (Tens).
     * This is a pedagogical decision owned by the Level, not the Exercise.
     *
     * Default implementation returns only the primary fact ID from the equation.
     */
    fun getAffectedFactIds(exercise: Exercise): List<String> = listOf(exercise.getFactId())

    /**
     * Returns true if this level is capable of generating the given equation.
     * Used to identify the level responsible for a specific exercise during mixed practice.
     */
    fun recognizes(equation: Equation): Boolean =
        getAllPossibleFactIds().contains(equation.getFactId())

    /**
     * Calculates the progress for this level based on user mastery.
     * Uses a non-linear weight model: Strength 4 = 50%, Strength 5 = 100%.
     */
    fun calculateProgress(masteryMap: Map<String, FactMastery>): Float {
        val factIds = getAllPossibleFactIds()
        if (factIds.isEmpty()) return 0f

        val totalWeight = factIds.sumOf { factId ->
            val strength = masteryMap[factId]?.strength ?: 0
            when (strength) {
                0 -> 0.0
                in 1..3 -> 0.1
                4 -> 0.5
                5 -> 1.0
                else -> 1.0
            }
        }
        return (totalWeight / factIds.size).toFloat()
    }

    // TODO: consider finding a better home for this function
    fun calculateStars(masteryMap: Map<String, FactMastery>): Int {
        val progress = calculateProgress(masteryMap)
        return (progress * 3).toInt()
    }
}

class MixedReviewLevel(
    override val id: String,
    override val operation: Operation,
    private val levelsToReview: List<Level>
) : Level {
    override val prerequisites: Set<String> = levelsToReview.map { it.id }.toSet()
    override val strategy = ExerciseStrategy.REVIEW

    override fun generateExercise(): Exercise {
        val randomLevel = levelsToReview.random()
        return randomLevel.generateExercise()
    }

    override fun getAllPossibleFactIds(): List<String> {
        return levelsToReview.flatMap { it.getAllPossibleFactIds() }
    }

    override fun recognizes(equation: Equation): Boolean {
        return levelsToReview.any { it.recognizes(equation) }
    }

}