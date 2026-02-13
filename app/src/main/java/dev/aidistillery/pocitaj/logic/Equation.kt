package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import java.util.Locale

interface Equation {
    // Displayed as the exercise question in the UI
    fun question(): String

    // Expected result, validated when a solution is submitted or as hint to stroke recognition.
    fun getExpectedResult(): Int

    // New helper function to get the basic equation string based on submitted solution
    fun getQuestionAsSolved(submittedSolution: Int?): String

    fun getFact(): Triple<Operation, Int, Int>

    // Unique identifier for mastery tracking
    fun getFactId(): String
}

data class Addition(private val a: Int, private val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d + %d = ?", a, b)
    override fun getExpectedResult(): Int = a + b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question() // If no solution, just show the question
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.ADDITION, a, b)
    override fun getFactId(): String = "$a + $b = ?"
}

data class Subtraction(private val a: Int, private val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d - %d = ?", a, b)
    override fun getExpectedResult(): Int = a - b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(Locale.ENGLISH, "%d - %d", a, b)
        } else {
            question() // If no solution, just show the question
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.SUBTRACTION, a, b)
    override fun getFactId(): String = "$a - $b = ?"
}

// TODO: Move operation to first parameter? Also, why not just factId, can't it all be reconstructed?
data class TwoDigitEquation(
    private val op1: Int,
    private val op2: Int,
    private val operation: Operation,
    private val factId: String
) : Equation {
    override fun question(): String = when (operation) {
        // TODO: Use operation.toSymbol
        Operation.ADDITION -> String.format(Locale.ENGLISH, "%d + %d = ?", op1, op2)
        Operation.SUBTRACTION -> String.format(Locale.ENGLISH, "%d - %d = ?", op1, op2)
        else -> throw IllegalArgumentException("Unsupported operation for TwoDigitEquation")
    }

    override fun getExpectedResult(): Int = when (operation) {
        Operation.ADDITION -> op1 + op2
        Operation.SUBTRACTION -> op1 - op2
        else -> throw IllegalArgumentException("Unsupported operation for TwoDigitEquation")
    }

    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            when (operation) {
                // TODO: Subtraction looks like a bug - it should do just the same as ADDITION.
                Operation.ADDITION -> question().replace("?", submittedSolution.toString())
                Operation.SUBTRACTION -> String.format(Locale.ENGLISH, "%d - %d", op1, op2)
                else -> throw IllegalArgumentException("Unsupported operation")
            }
        } else {
            question()
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(operation, op1, op2)
    override fun getFactId() = factId
}

data class Multiplication(val a: Int, val b: Int) : Equation {
    override fun question(): String =
        String.format(Locale.ENGLISH, "%d × %d = ?", a, b) // Using '×' for multiplication symbol

    override fun getExpectedResult(): Int = a * b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question() // If no solution, just show the question
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.MULTIPLICATION, a, b)

    // Using '*' for ID consistency with programming/math standard, distinct from 'x' or '×'
    override fun getFactId(): String = "$a * $b = ?"
}

data class MissingAddend(private val a: Int, private val result: Int) : Equation {
    private val b: Int = result - a // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d + ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(
                Locale.ENGLISH,
                "%d + %d = %d",
                a,
                submittedSolution,
                result
            ) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.ADDITION, a, b)
    override fun getFactId(): String = "$a + ? = $result"
}

data class MissingSubtrahend(private val a: Int, private val result: Int) : Equation {
    private val b: Int = a - result // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d - ? = %d", a, result)
    override fun getExpectedResult(): Int = b
    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            String.format(
                Locale.ENGLISH,
                "%d - %d = %d",
                a,
                submittedSolution,
                result
            ) // Incorporate submitted solution
        } else {
            question() // If no solution, just show the question
        }
    }

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.SUBTRACTION, a, b)
    override fun getFactId(): String = "$a - ? = $result"
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

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.DIVISION, a, b)
    override fun getFactId(): String = "$a / $b = ?"
}
