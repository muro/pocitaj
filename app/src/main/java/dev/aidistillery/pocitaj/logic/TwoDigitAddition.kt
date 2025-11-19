package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

data class TwoDigitAddition(
    val op1: Int,
    val op2: Int,
    private val ephemeralToken: String
) : Equation {
    override fun getFact(): Triple<Operation, Int, Int> {
        return Triple(Operation.ADDITION, op1, op2)
    }

    override fun getExpectedResult(): Int {
        return op1 + op2
    }

    override fun question(): String {
        return "$op1 + $op2 = ?"
    }

    override fun getQuestionAsSolved(submittedSolution: Int?): String {
        return if (submittedSolution != null) {
            question().replace("?", submittedSolution.toString())
        } else {
            question()
        }
    }

    fun getFactId(): String {
        return ephemeralToken
    }
}
