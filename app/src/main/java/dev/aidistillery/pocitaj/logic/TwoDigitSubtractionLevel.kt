package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation

// TODO: this class seems a bit too complicated, look later at fixing that.
open class TwoDigitSubtractionLevel(
    override val id: String,
    private val withBorrow: Boolean
) : Level {
    override val operation = Operation.SUBTRACTION
    override val prerequisites = setOf(Curriculum.SubtractingTens.id)
    override val strategy = ExerciseStrategy.DRILL

    override fun generateExercise(): Exercise {
        var op1: Int // Minuend
        var op2: Int // Subtrahend
        if (withBorrow) {
            do {
                op1 = kotlin.random.Random.nextInt(11, 100)
                op2 = kotlin.random.Random.nextInt(10, op1)
                // Need borrow means: ones digit of op1 < ones digit of op2
            } while ((op1 % 10) >= (op2 % 10))
        } else {
            do {
                op1 = kotlin.random.Random.nextInt(11, 100)
                op2 = kotlin.random.Random.nextInt(10, op1)
                // No borrow means: ones digit of op1 >= ones digit of op2
            } while ((op1 % 10) < (op2 % 10))
        }
        return Exercise(Subtraction(op1, op2))
    }

    // Decompose into facts like "SUB_ONES_o1_o2" and "SUB_TENS_t1_t2"
    // For 84 - 27 (Borrow): 
    // Ones: 14 - 7 (This is tricky. Addition logic was ADD_ONES_4_7 -> 11. Here it's 14-7?)
    // Actually, for consistency with TwoDigitAdditionDrillStrategy, we should track mastery of the components.
    // 
    // Case 1: No Borrow (84 - 23)
    // Ones: 4 - 3
    // Tens: 80 - 20 (or just 8-2 in tens column)
    //
    // Case 2: Borrow (84 - 27)
    // The mental model is usually:
    // Ones: 14 - 7
    // Tens: 70 - 20 (after borrowing) OR 80 - 20 - 10?
    //
    // To keep it simple and effective as a "Drill", we should map these to known facts.
    // Ones: "SUB_FROM_TEENS_14_7" or just re-use "SUB_FROM_14_7"
    // Tens: "SUB_TENS_70_20"

    override fun getAllPossibleFactIds(): List<String> {
        // Generate Ones Facts
        val ones = if (withBorrow) {
            // Case: Borrow (e.g. 14 - 7)
            // op1Ones < op2Ones.
            // op1Ones range: 0..8 (can't be 9 because op2Ones must be > op1Ones and <= 9)
            (0..8).flatMap { op1One ->
                (op1One + 1..9).map { op2One ->
                    // Fact ID format for borrow uses the "teen" number (e.g. 14)
                    "SUB_ONES_${op1One + 10}_${op2One}"
                }
            }
        } else {
            // Case: No Borrow (e.g. 4 - 3)
            // op1Ones >= op2Ones
            (0..9).flatMap { op1One ->
                (0..op1One).map { op2One ->
                    "SUB_ONES_${op1One}_${op2One}"
                }
            }
        }

        // Generate Tens Facts
        // We ensure op2 >= 10, so op2Tens >= 1.
        val tens = if (withBorrow) {
            // Effective tens are reduced by 1 because of the borrow.
            // Original tens (op1Tens) range: 1..9
            // Effective tens (op1TensEffective) range: 0..8
            // We need effectiveTens >= op2Tens for the result to be positive/zero.
            // And op2Tens >= 1 (since op2 is a two-digit number).
            (1..8).flatMap { effectiveOp1Tens ->
                (1..effectiveOp1Tens).map { op2Tens ->
                    "SUB_TENS_${effectiveOp1Tens}_${op2Tens}"
                }
            }
        } else {
            // No borrow, standard subtraction of tens digits.
            // op1Tens >= op2Tens >= 1
            (1..9).flatMap { op1Tens ->
                (1..op1Tens).map { op2Tens ->
                    "SUB_TENS_${op1Tens}_${op2Tens}"
                }
            }
        }

        // Combine
        return ones.flatMap { oneFact ->
            tens.map { tenFact ->
                "${oneFact}_${tenFact}"
            }
        }
    }
}
