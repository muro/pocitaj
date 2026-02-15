package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

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
        var factId: String

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
                factId = "$op1 + $op2 = ?"

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
                factId = "$op1 - $op2 = ?"
            }
            // If we are here, we have valid operands
            break
        } while (true)

        return Exercise(TwoDigitEquation(operation, op1, op2, factId))
    }

    override fun getAllPossibleFactIds(): List<String> {
        // MINIMAL FIX: Return component IDs instead of combinatorial IDs.
        val onesPairs = if (operation == Operation.ADDITION) {
            (0..9).flatMap { o1 ->
                (0..9).mapNotNull { o2 ->
                    val sum = o1 + o2
                    if ((withRegrouping && sum >= 10) || (!withRegrouping && sum < 10)) {
                        o1 to o2
                    } else null
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Borrow: o1 < o2
                (0..8).flatMap { o1 ->
                    (o1 + 1..9).map { o2 ->
                        o1 to o2
                    }
                }
            } else {
                // No borrow: o1 >= o2
                (0..9).flatMap { o1 ->
                    (0..o1).map { o2 ->
                        o1 to o2
                    }
                }
            }
        }

        val tensPairs = if (operation == Operation.ADDITION) {
            val maxTensSum = if (withRegrouping) 8 else 9
            (1..9).flatMap { t1 ->
                (1..(maxTensSum - t1)).map { t2 ->
                    t1 to t2
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Effective tens of op1 is (op1Tens - 1)
                // We iterate effective tens from 1..8
                // Fact stores effective tens: SUB_TENS_{effT1}_{t2}
                (1..8).flatMap { effT1 ->
                    (1..effT1).map { t2 ->
                        (effT1 + 1) to t2
                    }
                }
            } else {
                // No borrow
                (1..9).flatMap { t1 ->
                    (1..t1).map { t2 ->
                        t1 to t2
                    }
                }
            }
        }

        val prefix = if (operation == Operation.ADDITION) "ADD" else "SUB"

        val ones = onesPairs.map { (o1, o2) -> "${prefix}_ONES_${o1}_${o2}" }
        val tens = tensPairs.map { (t1, t2) -> "${prefix}_TENS_${t1}_${t2}" }

        return (ones + tens).distinct()
    }
}
