package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

// TODO: Look into this class and simplify.
open class TwoDigitComputationLevel(
    override val id: String,
    override val operation: Operation,
    private val withRegrouping: Boolean // carry (addition) or borrow (subtraction)
) : Level {

    override val prerequisites = if (operation == Operation.ADDITION) {
        setOf(Curriculum.AddingTens.id)
    } else {
        setOf(Curriculum.SubtractingTens.id)
    }
    override val strategy = ExerciseStrategy.DRILL

    override fun generateExercise(): Exercise {
        var op1: Int
        var op2: Int

        do {
            if (operation == Operation.ADDITION) {
                op1 = kotlin.random.Random.nextInt(10, 100)
                op2 = kotlin.random.Random.nextInt(10, 100)

                // Sum check
                if (op1 + op2 >= 100) continue

                val onesSum = (op1 % 10) + (op2 % 10)
                val conditionMet = if (withRegrouping) {
                    onesSum >= 10
                } else {
                    onesSum < 10
                }
                if (!conditionMet) continue

            } else { // SUBTRACTION
                op1 = kotlin.random.Random.nextInt(11, 100)
                // Ensure op2 has 2 digits (>=10) and result is positive (op2 <= op1)
                op2 = kotlin.random.Random.nextInt(10, op1 + 1)

                val ones1 = op1 % 10
                val ones2 = op2 % 10

                val conditionMet = if (withRegrouping) {
                    // Borrow needed: ones1 < ones2
                    ones1 < ones2
                } else {
                    // No borrow: ones1 >= ones2
                    ones1 >= ones2
                }
                if (!conditionMet) continue
            }
            // If we are here, we have valid operands
            break
        } while (true)

        return if (operation == Operation.ADDITION) {
            Exercise(Addition(op1, op2))
        } else {
            Exercise(Subtraction(op1, op2))
        }
    }

    override fun getAllPossibleFactIds(): List<String> {
        val factPrefix = if (operation == Operation.ADDITION) "ADD" else "SUB"

        val onesFacts = if (operation == Operation.ADDITION) {
            (0..9).flatMap { o1 ->
                (0..9).mapNotNull { o2 ->
                    val sum = o1 + o2
                    if ((withRegrouping && sum >= 10) || (!withRegrouping && sum < 10)) {
                        "${factPrefix}_ONES_${o1}_${o2}"
                    } else null
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Borrow: o1 < o2. Fact is HELD as 1{o1}, e.g. 14-7
                // The stored fact uses "14" and "7".
                (0..8).flatMap { o1 ->
                    (o1 + 1..9).map { o2 ->
                        "${factPrefix}_ONES_${o1 + 10}_${o2}"
                    }
                }
            } else {
                // No borrow: o1 >= o2
                (0..9).flatMap { o1 ->
                    (0..o1).map { o2 ->
                        "${factPrefix}_ONES_${o1}_${o2}"
                    }
                }
            }
        }

        val tensFacts = if (operation == Operation.ADDITION) {
            // Max tens sum is 8 if carrying (to avoid >100 result), 9 otherwise
            val maxTensSum = if (withRegrouping) 8 else 9
            (1..9).flatMap { t1 ->
                (1..(maxTensSum - t1)).map { t2 ->
                    "${factPrefix}_TENS_${t1}_${t2}"
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Effective tens of op1 is (op1Tens - 1)
                // We iterate effective tens from 1..8
                // Fact stores effective tens: SUB_TENS_{effT1}_{t2}
                (1..8).flatMap { effT1 ->
                    (1..effT1).map { t2 ->
                        "${factPrefix}_TENS_${effT1}_${t2}"
                    }
                }
            } else {
                // No borrow
                (1..9).flatMap { t1 ->
                    (1..t1).map { t2 ->
                        "${factPrefix}_TENS_${t1}_${t2}"
                    }
                }
            }
        }

        return onesFacts.flatMap { one ->
            tensFacts.map { ten -> "${one}_${ten}" }
        }
    }
}
