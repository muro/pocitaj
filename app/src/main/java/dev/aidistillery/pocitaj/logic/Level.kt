package dev.aidistillery.pocitaj.logic

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

    fun createExercise(equation: Equation): Exercise = Exercise(equation)
    fun createExercise(factId: String): Exercise = createExercise(Equation.parse(factId)!!)

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