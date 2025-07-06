package com.codinglikeapirate.pocitaj

import java.util.Locale
import java.util.Random


data class ExerciseConfig(val type: String, val upTo: Int = 10, val count: Int = 10)

enum class ExerciseType(val id: String) {
    ADDITION("addition"),
    MISSING_ADDEND("missing_addend"),
    SUBTRACTION("subtraction"),
    MISSING_SUBTRAHEND("missing_subtrahend"),
    MULTIPLICATION("multiplication")
}
// Holds a set of exercises to do in a session.
// Each Exercise can be checked for correct solution
class ExerciseBook {

    private val exercises = mutableListOf<Exercise>()
    private var currentIndex = -1

    fun getNextExercise(): Exercise? {
        currentIndex++
        return if (currentIndex < exercises.size) {
            exercises[currentIndex]
        } else {
            null
        }
    }

    /**
     * Clears the current session and loads a predefined list of exercises for testing.
     * This is the primary way to set up a predictable state for UI tests.
     */
    fun loadSession(predefinedExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(predefinedExercises)
        currentIndex = -1
    }

    // Function to handle exercise setup completion
    fun generateExercises(exerciseConfig: ExerciseConfig) { // You'll define ExerciseConfig
        if (exercises.isNotEmpty()) {
            return
        }
        // no clear, as we know exercises are empty
        currentIndex = -1

        val exerciseType: ExerciseType = when (exerciseConfig.type) {
            ExerciseType.ADDITION.id -> ExerciseType.ADDITION
            ExerciseType.MISSING_ADDEND.id -> ExerciseType.MISSING_ADDEND
            ExerciseType.SUBTRACTION.id -> ExerciseType.SUBTRACTION
            ExerciseType.MISSING_SUBTRAHEND.id -> ExerciseType.MISSING_SUBTRAHEND
            ExerciseType.MULTIPLICATION.id -> ExerciseType.MULTIPLICATION
            else -> {
                ExerciseType.ADDITION
            }
        }

        repeat(exerciseConfig.count) {
            generate(exerciseType, exerciseConfig.upTo)
        }
    }

    // This method will likely be removed or significantly changed
    // in favor of a more flexible generation strategy.
    fun generate(type: ExerciseType, bound: Int = 10): Exercise {
        // This generation logic will be moved elsewhere
        val equation = when (type) {
            ExerciseType.ADDITION -> {
                if (bound > 3 && Random().nextBoolean()) {
                    val a = Random().nextInt(bound)
                    val result = Random().nextInt(bound)
                    MissingAddend(a, result)
                } else {
                    val a = Random().nextInt(bound)
                    val b = Random().nextInt(bound)
                    Addition(a, b)
                }
            }
            ExerciseType.SUBTRACTION -> {
                if (bound > 3 && Random().nextBoolean()) {
                    val a = Random().nextInt(bound)
                    val result = Random().nextInt(a + 1)
                    MissingSubtrahend(a, result)
                } else {
                    val a = Random().nextInt(bound)
                    val b = Random().nextInt(a + 1)
                    Subtraction(a, b)
                }
            }
            ExerciseType.MULTIPLICATION -> {
                val a = Random().nextInt(bound)
                val b = Random().nextInt(bound)
                Multiplication(a, b)
            }
            else -> {
                // Fallback for other types, though they are not selectable in the UI anymore
                val a = Random().nextInt(bound)
                val b = Random().nextInt(bound)
                Addition(a, b)
            }
        }
        val exercise = Exercise(equation)
        exercises.add(exercise)
        return exercise
    }

    // Modify last to return SolvableExercise
    val last: Exercise
        get() = exercises.last()

    val stats: String
        get() {
            var solvedCount = 0
            var correctCount = 0
            for (solvableExercise in exercises) {
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

    val historyList: List<Exercise>
        get() = exercises.toList()
}



interface Equation {
    // Displayed as the exercise question in the UI
    fun question(): String
    // Expected result, validated when a solution is submitted or as hint to stroke recognition.
    fun getExpectedResult(): Int

    // New helper function to get the basic equation string based on submitted solution
    fun getQuestionAsSolved(submittedSolution: Int?): String
}

data class Addition(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d + %d = ?", a, b)
    override fun getExpectedResult(): Int = a + b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class Subtraction(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d - %d = ?", a, b)
    override fun getExpectedResult(): Int = a - b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d - %d", a, b)
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class Multiplication(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d × %d = ?", a, b) // Using '×' for multiplication symbol
    override fun getExpectedResult(): Int = a * b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class MissingAddend(val a: Int, val result: Int) : Equation {
    private val b: Int = result - a // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d + ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d + %d = %d", a, submittedSolution, result) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class MissingSubtrahend(val a: Int, val result: Int) : Equation {
    private val b: Int = a - result // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d - ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d - %d = %d", a, submittedSolution, result) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }
}

data class Exercise(
    val equation: Equation,
    var submittedSolution: Int? = null,
    var solved: Boolean = false,
    var timeTakenMillis: Int? = null
) {
    companion object {
        const val NOT_RECOGNIZED = -1000
    }

    fun correct(): Boolean {
        if (submittedSolution == NOT_RECOGNIZED) {
            return false
        }
        return solved && submittedSolution == equation.getExpectedResult()
    }

    fun solve(solution: Int, timeMillis: Int? = null): Boolean {
        this.submittedSolution = solution
        if (solution == NOT_RECOGNIZED) {
            return false
        }
        this.solved = true
        this.timeTakenMillis = timeMillis
        return correct()
    }

    fun equationString(): String {
        return when {
            solved && correct() -> {
                equation.question().replace("?", submittedSolution.toString())
            }
            solved && !correct() -> equation.question().replace("?", submittedSolution.toString()).replace("=", "≠")
            // submittedSolution == NOT_RECOGNIZED -> "${equation.question()} ≠ ?" // Special case for not recognized
            else -> equation.question() // Not solved, show just the question
        }
    }
}