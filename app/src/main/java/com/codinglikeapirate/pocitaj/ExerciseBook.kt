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
        private const val BOUND = 10
    }

    private val random = Random() //1234)
    private val history = mutableListOf<Exercise>()

    fun generate(type: ExerciseType, bound: Int = BOUND) : Exercise {
        val exercise = when (type) {
            ExerciseType.ADDITION -> {
                val a = random.nextInt(bound)
                val b = random.nextInt(bound)
                Addition(a, b)
            }
            ExerciseType.SUBTRACTION -> {
                val a = random.nextInt(bound)
                val b = random.nextInt(a + 1) // Ensure result is non-negative
                Subtraction(a, b)
            }
            // If you add more ExerciseType values, you'll need to handle them here
        }

        history.add(exercise)
        return exercise
    }

    fun clear() {
        history.clear()
    }

    val last: Exercise
        get() = history.last()

    val stats: String
        get() {
            var solved = 0
            var correct = 0
            for (a in history) {
                if (a.solved()) {
                    solved++
                }
                if (a.correct()) {
                    correct++
                }
            }
            val percent = if (solved != 0) 100f * correct / solved.toFloat() else 0f
            return String.format(Locale.ENGLISH, "%d / %d (%.0f%%)", correct, solved, percent)
        }

    val historyList: List<Exercise>
        get() = history.toList()

    interface Exercise {
        // Returns the Exercise question only, without a solution, as a string
        fun question(): String

        // Returns the Exercise question with the submitted solution as a string.
        // When the solution was solved incorrectly, it uses the not equal sign.
        // If the solution is not solved yet, returns the question without a solution.
        fun equation(): String

        fun getExpectedResult(): Int

        // Marks the Exercise as solved and returns true if the solution is correct.
        // If the proposed solution is NOT_RECOGNIZED, doesn't set it as solved.
        fun solve(solution: Int): Boolean

        // Returns true, if the proposed solution is correct
        fun check(solution: Int): Boolean

        // Returns true, if the Exercise has been correctly or incorrectly solved.
        fun solved(): Boolean

        // Returns true if the Exercise is solved and correct.
        fun correct(): Boolean
    }

    class Addition(private val a: Int, private val b: Int) : Exercise {
        private var solution: Int = 0
        private var solved: Boolean = false

        override fun question(): String {
            return String.format(Locale.ENGLISH, "%d + %d", a, b)
        }

        override fun solve(solution: Int): Boolean {
            this.solution = solution
            if (solution == NOT_RECOGNIZED) {
                return false
            }
            // only set solved, if it's not the default:
            this.solved = true
            return correct()
        }

        override fun check(solution: Int): Boolean {
            return solution == getExpectedResult()
        }

        override fun solved(): Boolean {
            return solved
        }

        override fun correct(): Boolean {
            return solved && check(solution)
        }

        override fun getExpectedResult(): Int {
            return a + b
        }

        override fun equation(): String {
            return if (correct()) {
                String.format(Locale.ENGLISH, "%d + %d = %d", a, b, solution)
            } else {
                if (solution == NOT_RECOGNIZED) {
                    String.format(Locale.ENGLISH, "%d + %d ≠ ?", a, b)
                } else {
                    if (solved) {
                        String.format(Locale.ENGLISH, "%d + %d ≠ %d", a, b, solution)
                    } else {
                        question()
                    }
                }
            }
        }
    }

    class Subtraction(private val a: Int, private val b: Int) : Exercise {
        private var solution: Int = 0
        private var solved: Boolean = false

        override fun question(): String {
            return String.format(Locale.ENGLISH, "%d - %d", a, b)
        }

        override fun solve(solution: Int): Boolean {
            this.solution = solution
            if (solution == NOT_RECOGNIZED) {
                return false
            }
            // only set solved, if it's not the default:
            this.solved = true
            return correct()
        }

        override fun check(solution: Int): Boolean {
            return solution == getExpectedResult()
        }

        override fun solved(): Boolean {
            return solved
        }

        override fun correct(): Boolean {
            return solved && check(solution)
        }

        override fun getExpectedResult(): Int {
            return a - b
        }

        override fun equation(): String {
            return if (correct()) {
                String.format(Locale.ENGLISH, "%d - %d = %d", a, b, solution)
            } else {
                if (solution == NOT_RECOGNIZED) {
                    String.format(Locale.ENGLISH, "%d - %d ≠ ?", a, b)
                } else {
                    if (solved) {
                        String.format(Locale.ENGLISH, "%d - %d ≠ %d", a, b, solution)
                    } else {
                        question()
                    }
                }
            }
        }
    }
}