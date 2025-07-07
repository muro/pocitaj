package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation

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
            else -> equation.question()
        }
    }

    fun getFactId(): String {
        val op = when(equation) {
            is Addition -> Operation.ADDITION
            is Subtraction -> Operation.SUBTRACTION
            is Multiplication -> Operation.MULTIPLICATION
            is MissingAddend -> Operation.ADDITION // Or a new operation type
            is MissingSubtrahend -> Operation.SUBTRACTION // Or a new operation type
            else -> throw IllegalStateException("Unknown equation type")
        }
        val (op1, op2) = when(equation) {
            is Addition -> Pair(equation.a, equation.b)
            is Subtraction -> Pair(equation.a, equation.b)
            is Multiplication -> Pair(equation.a, equation.b)
            is MissingAddend -> Pair(equation.a, equation.getExpectedResult())
            is MissingSubtrahend -> Pair(equation.a, equation.getExpectedResult())
            else -> throw IllegalStateException("Unknown equation type")
        }
        return "${op.name}_${op1}_${op2}"
    }
}
