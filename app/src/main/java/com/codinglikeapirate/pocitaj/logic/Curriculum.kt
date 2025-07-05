package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.Operation
import kotlin.random.Random

/**
 * A singleton object that holds the entire curriculum for the app.
 */
object Curriculum {

    // --- Level Definitions ---

    object SumsUpTo5 : Level {
        override val id = "ADD_SUM_5"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, 6)
            val op2 = Random.nextInt(0, 6 - op1)
            return Exercise(op1, op2, op1 + op2, operation)
        }
    }

    object SumsUpTo10 : Level {
        override val id = "ADD_SUM_10"
        override val operation = Operation.ADDITION

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, 11)
            val op2 = Random.nextInt(0, 11 - op1)
            return Exercise(op1, op2, op1 + op2, operation)
        }
    }

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        return listOf(SumsUpTo5, SumsUpTo10)
    }
}
