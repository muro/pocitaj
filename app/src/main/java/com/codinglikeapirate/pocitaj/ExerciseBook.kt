package com.codinglikeapirate.pocitaj

import java.util.Locale
import java.util.Random

class ExerciseBook {

    companion object {
        const val NOT_RECOGNIZED = -1000
        private const val BOUND = 10
    }

    private val random = Random() //1234)
    private val history = mutableListOf<Addition>()

    init {
        generate()
    }

    private fun generate(bound: Int = BOUND): Addition {
        return Addition(random.nextInt(bound), random.nextInt(bound))
    }

    fun generate() {
        history.add(generate(BOUND))
    }

    val last: Addition
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
        // Returns the Exercise question as a string
        fun question(): String

        // Marks the Exercise as solved and returns true if the solution is correct.
        // If the proposed solution is NOT_RECOGNIZED, doesn't set it as solved.
        fun solve(solution: Int): Boolean

        // Returns true, if the Exercise has been solved.
        fun solved(): Boolean

        // Returns true if the Exercise has been correctly solved.
        fun correct(): Boolean

        // Returns the full equation as a string.
        fun equation(): String
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

        override fun solved(): Boolean {
            return solved
        }

        override fun correct(): Boolean {
            return solved && a + b == solution
        }

        fun getExpectedResult(): Int {
            return a + b
        }

        override fun equation(): String {
            return if (correct()) {
                String.format(Locale.ENGLISH, "%d + %d = %d", a, b, solution)
            } else {
                if (solution == NOT_RECOGNIZED) {
                    String.format(Locale.ENGLISH, "%d + %d ≠ ?", a, b)
                } else {
                    String.format(Locale.ENGLISH, "%d + %d ≠ %d", a, b, solution)
                }
            }
        }
    }
}