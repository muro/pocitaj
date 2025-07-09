package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation
import kotlin.random.Random

/**
 * A singleton object that holds the entire curriculum for the app.
 */
object Curriculum {

    // --- Level Definitions ---

    private object SumsUpTo5 : Level {
        override val id = "ADD_SUM_5"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, 6)
            val op2 = Random.nextInt(0, 6 - op1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..5).flatMap { op1 ->
                (0..(5 - op1)).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SumsUpTo10 : Level {
        override val id = "ADD_SUM_10"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, 11)
            val op2 = Random.nextInt(0, 11 - op1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            // This logic needs to be more robust to handle the exclusive nature
            // of the level. For now, this is a simplified placeholder.
            return (0..10).flatMap { op1 ->
                (0..(10 - op1)).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object SubtractionFrom5 : Level {
        override val id = "SUB_FROM_5"
        override val operation = Operation.SUBTRACTION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, 6)
            val op2 = Random.nextInt(0, op1 + 1)
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..5).flatMap { op1 ->
                (0..op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object MultiplicationTables012510 : Level {
        override val id = "MUL_TABLES_0_1_2_5_10"
        override val operation = Operation.MULTIPLICATION
        private val tables = listOf(0, 1, 2, 5, 10)

        override fun generateExercise(): Exercise {
            val op1 = tables.random()
            val op2 = Random.nextInt(0, 11)
            return Exercise(Multiplication(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return tables.flatMap { op1 ->
                (0..10).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    private object DivisionBy2510 : Level {
        override val id = "DIV_BY_2_5_10"
        override val operation = Operation.DIVISION
        private val divisors = listOf(2, 5, 10)

        override fun generateExercise(): Exercise {
            val op2 = divisors.random()
            val result = Random.nextInt(0, 11)
            val op1 = op2 * result
            return Exercise(Division(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return divisors.flatMap { op2 ->
                (0..10).map { result ->
                    val op1 = op2 * result
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        return listOf(
            SumsUpTo5,
            SumsUpTo10,
            SubtractionFrom5,
            MultiplicationTables012510,
            DivisionBy2510
        )
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