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

    private class MultiplicationTableLevel(private val table: Int) : Level {
        override val id = "MUL_TABLE_$table"
        override val operation = Operation.MULTIPLICATION

        override fun generateExercise(): Exercise {
            var op1 = table
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
            (0..10).forEach { op2 ->
                facts.add("${operation.name}_${table}_${op2}")
                facts.add("${operation.name}_${op2}_${table}")
            }
            return facts.toList()
        }
    }

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
        val divisionLevels = (1..12).map { DivisionTableLevel(it) }

        return listOf(
            SumsUpTo5,
            SumsUpTo10,
            SubtractionFrom5,
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
