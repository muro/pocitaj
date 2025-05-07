package com.codinglikeapirate.pocitaj

import java.util.Locale
import java.util.Random


enum class ExerciseType {
    ADDITION,
    SUBTRACTION
}
// Holds a set of exercises to do in a session.
// Each Exercise can be checked for correct solution
class ExerciseBook {

    companion object {
        const val NOT_RECOGNIZED = -1000
    }

    private val history = mutableListOf<SolvableExercise>() // Store SolvableExercise

    fun clear() {
        history.clear()
    }

    // This method will likely be removed or significantly changed
    // in favor of a more flexible generation strategy.
    fun generate(type: ExerciseType, bound: Int = 10): SolvableExercise {
        // This generation logic will be moved elsewhere
        val equation = when (type) {
            ExerciseType.ADDITION -> {
                val a = Random().nextInt(bound)
                val b = Random().nextInt(bound)
                Addition(a, b)
            }

            ExerciseType.SUBTRACTION -> {
                val a = Random().nextInt(bound)
                val b = Random().nextInt(a + 1)
                Subtraction(a, b)
            }
        }
        val solvableExercise = SolvableExercise(equation)
        history.add(solvableExercise)
        return solvableExercise
    }

    // Modify last to return SolvableExercise
    val last: SolvableExercise
        get() = history.last()

    val stats: String
        get() {
            var solvedCount = 0
            var correctCount = 0
            for (solvableExercise in history) {
                if (solvableExercise.solved) { // Access 'solved' property directly
                    solvedCount++
                }
                if (solvableExercise.correct()) { // Call 'correct()' method
                    correctCount++
                }
            }
            val percent = if (solvedCount != 0) 100f * correctCount / solvedCount.toFloat() else 0f
            return String.format(
                Locale.ENGLISH,
                "%d / %d (%.0f%%)",
                correctCount,
                solvedCount,
                percent
            )
        }

    val historyList: List<SolvableExercise>
        get() = history.toList()
}



interface Equation {
    fun question(): String
    fun getExpectedResult(): Int

    // New helper function to get the basic equation string based on submitted solution
    fun getEquationString(submittedSolution: Int?): String
}

data class Addition(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d + %d", a, b)
    override fun getExpectedResult(): Int = a + b
    override fun getEquationString(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d + %d", a, b)
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class Subtraction(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d - %d", a, b)
    override fun getExpectedResult(): Int = a - b
    override fun getEquationString(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d - %d", a, b)
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class MissingAddend(val a: Int, val result: Int) : Equation {
    val b: Int = result - a // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d + ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getEquationString(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d + %d = %d", a, submittedSolution, result) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class MissingSubtrahend(val a: Int, val result: Int) : Equation {
    val b: Int = a - result // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d - ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getEquationString(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d - %d = %d", a, submittedSolution, result) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class SolvableExercise(
    val exercise: Equation,
    var submittedSolution: Int? = null,
    var solved: Boolean = false,
    var timeTakenMillis: Long? = null
) {
    fun correct(): Boolean {
        if (submittedSolution == ExerciseBook.NOT_RECOGNIZED) {
            return false
        }
        return solved && submittedSolution == exercise.getExpectedResult()
    }

    fun solve(solution: Int, timeMillis: Long? = null): Boolean {
        this.submittedSolution = solution
        if (solution == ExerciseBook.NOT_RECOGNIZED) {
            return false
        }
        this.solved = true
        this.timeTakenMillis = timeMillis
        return correct()
    }

    fun equation(): String {
        val baseEquation = exercise.getEquationString(submittedSolution)

        return when {
            solved && correct() -> "$baseEquation = $submittedSolution"
            solved && !correct() -> "$baseEquation ≠ $submittedSolution"
            submittedSolution == ExerciseBook.NOT_RECOGNIZED -> "${exercise.question()} ≠ ?" // Special case for not recognized
            else -> exercise.question() // Not solved, show just the question
        }
    }
}
