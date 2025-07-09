package com.codinglikeapirate.pocitaj.logic

import java.util.Locale

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

data class Division(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d ÷ %d = ?", a, b)
    override fun getExpectedResult(): Int = a / b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question()
        }
    }
}
