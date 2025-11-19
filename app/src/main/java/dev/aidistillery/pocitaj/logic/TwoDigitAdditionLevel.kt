package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

open class TwoDigitAdditionLevel(
    override val id: String,
    private val withCarry: Boolean
) : Level {
    override val operation = Operation.ADDITION
    override val prerequisites = setOf(Curriculum.AddingTens.id)
    override val strategy = ExerciseStrategy.DRILL

    override fun generateExercise(): Exercise {
        var op1: Int
        var op2: Int
        if (withCarry) {
            do {
                op1 = kotlin.random.Random.nextInt(10, 100)
                op2 = kotlin.random.Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) < 10 || op1 + op2 >= 100)
        } else {
            do {
                op1 = kotlin.random.Random.nextInt(10, 100)
                op2 = kotlin.random.Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) >= 10 || op1 + op2 >= 100)
        }
        return Exercise(Addition(op1, op2))
    }

    // TODO: this generates separate facts IDs for ones and tens
    override fun getAllPossibleFactIds(): List<String> {
        val ones = (0..9).flatMap { op1 ->
            (0..9).mapNotNull { op2 ->
                val sum = op1 + op2
                if ((withCarry && sum >= 10 && sum < 20) || (!withCarry && sum < 10)) {
                    "ADD_ONES_${op1}_${op2}"
                } else {
                    null
                }
            }
        }

        // Adjust the maximum sum for tens digits based on whether carrying is involved,
        // to prevent generating combinations that result in a sum >= 100.
        val maxTensSum = if (withCarry) 8 else 9

        val tens = (1..9).flatMap { op1 ->
            (1..(maxTensSum - op1)).map { op2 ->
                "ADD_TENS_${op1}_${op2}"
            }
        }

        // Create ephemeral tokens by combining all possible ones and tens.
        return ones.flatMap { oneFact ->
            tens.map { tenFact ->
                "${oneFact}_${tenFact}"
            }
        }
    }
}