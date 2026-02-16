package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import java.util.Locale

interface Equation {
    // Displayed as the exercise question in the UI
    fun question(): String

    // Expected result, validated when a solution is submitted or as hint to stroke recognition.
    fun getExpectedResult(): Int

    // New helper function to get the basic equation string based on submitted solution
    fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question() // If no solution, just show the question
        }
    }

    fun getFact(): Triple<Operation, Int, Int>

    // Unique identifier for mastery tracking
    fun getFactId(): String

    companion object {
        fun parse(factId: String): Equation? {
            // 1. Standard Arithmetic (5 + 3 = ?, 10 - 4 = ?, 7 * 6 = ?, 20 / 5 = ?)
            val standardRegex = Regex("""(\d+) ([+\-*/]) (\d+) = \?""")
            standardRegex.matchEntire(factId)?.let { match ->
                val (aStr, opSymbol, bStr) = match.destructured
                val a = aStr.toInt()
                val b = bStr.toInt()
                return when (opSymbol) {
                    "+" -> Addition(a, b)
                    "-" -> Subtraction(a, b)
                    "*" -> Multiplication(a, b)
                    "/" -> Division(a, b)
                    else -> null
                }
            }

            // 2. Missing Addend (5 + ? = 12)
            val missingAddRegex = Regex("""(\d+) \+ \? = (\d+)""")
            missingAddRegex.matchEntire(factId)?.let { match ->
                val (a, sum) = match.destructured
                return MissingAddend(a.toInt(), sum.toInt())
            }

            // 3. Missing Subtrahend (10 - ? = 4)
            val missingSubRegex = Regex("""(\d+) - \? = (\d+)""")
            missingSubRegex.matchEntire(factId)?.let { match ->
                val (a, diff) = match.destructured
                return MissingSubtrahend(a.toInt(), diff.toInt())
            }

            return null
        }
    }
}

data class Addition(private val a: Int, private val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d + %d = ?", a, b)
    override fun getExpectedResult(): Int = a + b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.ADDITION, a, b)
    override fun getFactId(): String = "$a + $b = ?"
}

data class Subtraction(private val a: Int, private val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d - %d = ?", a, b)
    override fun getExpectedResult(): Int = a - b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.SUBTRACTION, a, b)
    override fun getFactId(): String = "$a - $b = ?"
}




data class Multiplication(val a: Int, val b: Int) : Equation {
    override fun question(): String =
        String.format(Locale.ENGLISH, "%d × %d = ?", a, b) // Using '×' for multiplication symbol

    override fun getExpectedResult(): Int = a * b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.MULTIPLICATION, a, b)

    // Using '*' for ID consistency with programming/math standard, distinct from 'x' or '×'
    override fun getFactId(): String = "$a * $b = ?"
}

data class MissingAddend(private val a: Int, private val result: Int) : Equation {
    private val b: Int = result - a // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d + ? = %d", a, result)
    override fun getExpectedResult(): Int = b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.ADDITION, a, b)
    override fun getFactId(): String = "$a + ? = $result"
}

data class MissingSubtrahend(private val a: Int, private val result: Int) : Equation {
    private val b: Int = a - result // The missing operand

    override fun question(): String = String.format(Locale.ENGLISH, "%d - ? = %d", a, result)
    override fun getExpectedResult(): Int = b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.SUBTRACTION, a, b)
    override fun getFactId(): String = "$a - ? = $result"
}

data class Division(val a: Int, val b: Int) : Equation {
    override fun question(): String = String.format(Locale.ENGLISH, "%d ÷ %d = ?", a, b)
    override fun getExpectedResult(): Int = a / b

    override fun getFact(): Triple<Operation, Int, Int> = Triple(Operation.DIVISION, a, b)
    override fun getFactId(): String = "$a / $b = ?"
}

fun Operation.createEquation(op1: Int, op2: Int): Equation {
    return when (this) {
        Operation.ADDITION -> Addition(op1, op2)
        Operation.SUBTRACTION -> Subtraction(op1, op2)
        Operation.MULTIPLICATION -> Multiplication(op1, op2)
        Operation.DIVISION -> Division(op1, op2)
    }
}
