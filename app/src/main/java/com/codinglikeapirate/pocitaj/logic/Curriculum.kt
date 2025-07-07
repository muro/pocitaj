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
            return Exercise(op1, op2, op1 + op2, operation)
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
            return Exercise(op1, op2, op1 + op2, operation)
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

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        return listOf(SumsUpTo5, SumsUpTo10)
    }

    fun getLevelForExercise(exercise: Exercise): Level? {
        val factId = "${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}"
        return getAllLevels().find { level ->
            level.getAllPossibleFactIds().contains(factId)
        }
    }
}