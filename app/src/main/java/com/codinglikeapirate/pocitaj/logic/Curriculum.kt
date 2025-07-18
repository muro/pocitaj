package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation
import kotlin.random.Random

/**
 * A singleton object that holds the entire curriculum for the app.
 */
object Curriculum {

    // --- Level Definitions ---

    // --- Addition ---
    object SumsUpTo5 : Level {
        override val id = "ADD_SUM_5"
        override val operation = Operation.ADDITION
        private const val max_sum = 5

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SumsUpTo10 : Level {
        override val id = "ADD_SUM_10"
        override val operation = Operation.ADDITION
        private const val max_sum = 10

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SumsUpTo20 : Level {
        override val id = "ADD_SUM_20"
        override val operation = Operation.ADDITION
        private const val max_sum = 20

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object AddingTens : Level {
        override val id = "ADD_TENS"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(1, 10) * 10
            val op2 = Random.nextInt(1, 10) * 10
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).flatMap { op1 ->
                (1..9).map { op2 ->
                    "${operation.name}_${op1 * 10}_${op2 * 10}"
                }
            }
        }
    }

    private object TwoDigitAdditionNoCarry : Level {
        override val id = "ADD_TWO_DIGIT_NO_CARRY"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(10, 100)
                op2 = Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) >= 10 || op1 + op2 >= 100)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (10..99).flatMap { op1 ->
                (10..99).mapNotNull { op2 ->
                    if ((op1 % 10) + (op2 % 10) < 10 && op1 + op2 < 100) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    private object TwoDigitAdditionWithCarry : Level {
        override val id = "ADD_TWO_DIGIT_CARRY"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(10, 100)
                op2 = Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) < 10 || op1 + op2 >= 100)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (10..99).flatMap { op1 ->
                (10..99).mapNotNull { op2 ->
                    if ((op1 % 10) + (op2 % 10) >= 10 && op1 + op2 < 100) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    // --- Subtraction ---
    private object SubtractionFrom5 : Level {
        override val id = "SUB_FROM_5"
        override val operation = Operation.SUBTRACTION
        private const val max_minuend = 5

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_minuend + 1)
            val op2 = Random.nextInt(0, op1 + 1)
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_minuend).flatMap { op1 ->
                (0..op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SubtractionFrom10 : Level {
        override val id = "SUB_FROM_10"
        override val operation = Operation.SUBTRACTION
        private const val max_minuend = 10

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_minuend + 1)
            val op2 = Random.nextInt(0, op1 + 1)
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_minuend).flatMap { op1 ->
                (0..op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SubtractionFrom20 : Level {
        override val id = "SUB_FROM_20"
        override val operation = Operation.SUBTRACTION
        private const val max_minuend = 20

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_minuend + 1)
            val op2 = Random.nextInt(0, op1 + 1)
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_minuend).flatMap { op1 ->
                (0..op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SubtractingTens : Level {
        override val id = "SUB_TENS"
        override val operation = Operation.SUBTRACTION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(2, 10) * 10
            val op2 = Random.nextInt(1, op1 / 10) * 10
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (2..9).flatMap { op1Tens ->
                (1 until op1Tens).map { op2Tens ->
                    "${operation.name}_${op1Tens * 10}_${op2Tens * 10}"
                }
            }
        }
    }

    private object TwoDigitSubtractionNoBorrow : Level {
        override val id = "SUB_TWO_DIGIT_NO_BORROW"
        override val operation = Operation.SUBTRACTION

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(11, 100)
                op2 = Random.nextInt(10, op1)
            } while ((op1 % 10) < (op2 % 10))
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (11..99).flatMap { op1 ->
                (10 until op1).mapNotNull { op2 ->
                    if ((op1 % 10) >= (op2 % 10)) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    private object TwoDigitSubtractionWithBorrow : Level {
        override val id = "SUB_TWO_DIGIT_BORROW"
        override val operation = Operation.SUBTRACTION

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(11, 100)
                op2 = Random.nextInt(10, op1)
            } while ((op1 % 10) >= (op2 % 10))
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (11..99).flatMap { op1 ->
                (10 until op1).mapNotNull { op2 ->
                    if ((op1 % 10) < (op2 % 10)) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }


    // --- Multiplication ---
    private class MultiplicationTableLevel(private val table: Int) : Level {
        override val id = "MUL_TABLE_$table"
        override val operation = Operation.MULTIPLICATION

        override fun generateExercise(): Exercise {
            var op1 = table
            var op2 = Random.nextInt(0, 13)
            if (Random.nextBoolean()) {
                val temp = op1
                op1 = op2
                op2 = temp
            }
            return Exercise(Multiplication(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            val facts = mutableSetOf<String>()
            (0..12).forEach { op2 ->
                facts.add("${operation.name}_${table}_${op2}")
                facts.add("${operation.name}_${op2}_${table}")
            }
            return facts.toList()
        }
    }

    // --- Division ---
    private class DivisionTableLevel(private val divisor: Int) : Level {
        override val id = "DIV_BY_$divisor"
        override val operation = Operation.DIVISION

        override fun generateExercise(): Exercise {
            val result = Random.nextInt(0, 11)
            val op1 = divisor * result
            return Exercise(Division(op1, divisor))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..10).map { result ->
                val op1 = divisor * result
                "${operation.name}_${op1}_${divisor}"
            }
        }
    }

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        val multiplicationLevels = (0..12).map { MultiplicationTableLevel(it) }
        val divisionLevels = (1..10).map { DivisionTableLevel(it) }

        return listOf(
            // Addition
            SumsUpTo5,
            SumsUpTo10,
            SumsUpTo20,
            AddingTens,
            TwoDigitAdditionNoCarry,
            TwoDigitAdditionWithCarry,
            // Subtraction
            SubtractionFrom5,
            SubtractionFrom10,
            SubtractionFrom20,
            SubtractingTens,
            TwoDigitSubtractionNoBorrow,
            TwoDigitSubtractionWithBorrow,
        ) + multiplicationLevels + divisionLevels
    }

    fun getLevelForExercise(exercise: Exercise): Level? {
        val factId = exercise.getFactId()
        return getAllLevels().find { level ->
            level.getAllPossibleFactIds().contains(factId)
        }
    }

    fun getLevelsFor(operation: Operation): List<Level> {
        return getAllLevels().filter { it.operation == operation }
    }
}
