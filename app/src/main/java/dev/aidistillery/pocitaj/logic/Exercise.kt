package dev.aidistillery.pocitaj.logic

data class Exercise(
    val equation: Equation,
    var submittedSolution: Int? = null,
    var solved: Boolean = false,
    var timeTakenMillis: Int? = null,
    var speedBadge: SpeedBadge = SpeedBadge.NONE
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
        timeMillis?.let {
            val (op, op1, op2) = equation.getFact()
            this.speedBadge = getSpeedBadge(op, op1, op2, it.toLong())
        }
        return correct()
    }

    fun equationString(): String {
        return when {
            solved && correct() -> {
                equation.question().replace("?", submittedSolution.toString())
            }

            solved && !correct() -> equation.question().replace("?", submittedSolution.toString())
                .replace("=", "≠")

            else -> equation.question()
        }
    }

    fun getFactId(): String {
        val (op, op1, op2) = equation.getFact()
        return "${op.name}_${op1}_${op2}"
    }
}