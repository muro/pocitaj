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
        private const val max_sum = 5

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum).map { op2 ->
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
            val op2 = Random.nextInt(0, max_sum + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

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

    private object MultiplicationTables012510 : Level {
        override val id = "MUL_TABLES_0_1_2_5_10"
        override val operation = Operation.MULTIPLICATION
        private val tables = listOf(0, 1, 2, 5, 10)

        override fun generateExercise(): Exercise {
            var op1 = tables.random()
            var op2 = Random.nextInt(0, 11)
            if (Random.nextBoolean()) {
                val temp = op1
                op1 = op2
                op2 = temp
            }
            return Exercise(Multiplication(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            val facts = mutableSetOf<String>()
            tables.forEach { op1 ->
                (0..10).forEach { op2 ->
                    facts.add("${operation.name}_${op1}_${op2}")
                    facts.add("${operation.name}_${op2}_${op1}")
                }
            }
            return facts.toList()
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