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

        val equation =
            if (operation == Operation.ADDITION) Addition(op1, op2) else Subtraction(op1, op2)
        return createExercise(equation)
    }

    override fun getAllPossibleFactIds(): List<String> {
        val onesFacts = if (operation == Operation.ADDITION) {
            (0..9).flatMap { o1 ->
                (0..9).mapNotNull { o2 ->
                    val sum = o1 + o2
                    if ((withRegrouping && sum >= 10) || (!withRegrouping && sum < 10)) {
                        "$o1 + $o2 = ?"
                    } else null
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Borrow: o1 < o2
                (0..8).flatMap { o1 ->
                    (o1 + 1..9).map { o2 ->
                        "$o1 - $o2 = ?"
                    }
                }
            } else {
                // No borrow: o1 >= o2
                (0..9).flatMap { o1 ->
                    (0..o1).map { o2 ->
                        "$o1 - $o2 = ?"
                    }
                }
            }
        }

        val tensFacts = if (operation == Operation.ADDITION) {
            val maxTensSum = if (withRegrouping) 8 else 9
            (1..9).flatMap { t1 ->
                (1..(maxTensSum - t1)).map { t2 ->
                    "${t1 * 10} + ${t2 * 10} = ?"
                }
            }
        } else { // SUBTRACTION
            if (withRegrouping) {
                // Effective tens of op1 is (op1Tens - 1)
                (1..8).flatMap { effT1 ->
                    (1..effT1).flatMap { t2 ->
                        val list = mutableListOf("${(effT1 + 1) * 10} - ${t2 * 10} = ?")
                        if (effT1 > t2) {
                            list.add("${effT1 * 10} - ${t2 * 10} = ?")
                        }
                        list
                    }
                }
            } else {
                (1..9).flatMap { t1 ->
                    (1..t1).map { t2 ->
                        "${t1 * 10} - ${t2 * 10} = ?"
                    }
                }
            }
        }

        return (onesFacts + tensFacts).distinct()
    }

    override fun createExercise(factId: String): Exercise {
        val baseEquation = Equation.parse(factId) ?: return super.createExercise(factId)
        val (op, a, b) = baseEquation.getFact()

        // Embellish component facts into full 2-digit problems for the user
        return if (a < 10 && b < 10) {
            // Ones component: add tens that don't regroup themselves (unless level is carry/borrow)
            var op1: Int
            var op2: Int
            do {
                val t1 = kotlin.random.Random.nextInt(1, 9)
                val t2 = kotlin.random.Random.nextInt(1, 10 - t1)
                op1 = t1 * 10 + a
                op2 = t2 * 10 + b
                if (op == Operation.SUBTRACTION && op1 < op2) continue
                break
            } while (true)
            createExercise(
                if (op == Operation.ADDITION) Addition(op1, op2) else Subtraction(
                    op1,
                    op2
                )
            )
        } else if (a % 10 == 0 && b % 10 == 0) {
            // Tens component: add ones that match the level's carry/borrow rule
            var op1: Int
            var op2: Int
            do {
                val o1 = kotlin.random.Random.nextInt(0, 10)
                val o2 = kotlin.random.Random.nextInt(0, 10)
                val sum = o1 + o2
                val conditionMet = if (withRegrouping) {
                    if (op == Operation.ADDITION) sum >= 10 else o1 < o2
                } else {
                    if (op == Operation.ADDITION) sum < 10 else o1 >= o2
                }
                if (!conditionMet) continue
                op1 = a + o1
                op2 = b + o2
                if (op == Operation.SUBTRACTION && (op1 < op2 || op1 < 10 || op2 < 10)) continue
                break
            } while (true)
            createExercise(
                if (op == Operation.ADDITION) Addition(op1, op2) else Subtraction(
                    op1,
                    op2
                )
            )
        } else {
            super.createExercise(factId)
        }
    }

    override fun recognizes(equation: Equation): Boolean {
        val (op, A, B) = equation.getFact()
        if (op != operation) return false

        // Basic range check for two-digit numbers (10..99)
        if (A < 10 || A > 99) return false
        if (B < 10 || B > 99) return false

        if (operation == Operation.ADDITION) {
            // Level constraint: Sum < 100
            if (A + B >= 100) return false
        } else {
            // Subtraction: Result positive implies A >= B
            if (A < B) return false
        }

        // Check regrouping condition
        val startOnes = A % 10
        val operandOnes = B % 10

        return if (operation == Operation.ADDITION) {
            val sumOnes = startOnes + operandOnes
            if (withRegrouping) sumOnes >= 10 else sumOnes < 10
        } else {
            // Subtraction
            if (withRegrouping) startOnes < operandOnes else startOnes >= operandOnes
        }
    }

    /**
     * Decomposes the 2-digit exercise into its column-wise components.
     * E.g., `19 + 19` affects mastery for:
     * 1. The exercise itself (`19 + 19`)
     * 2. The ones column (`9 + 9`)
     * 3. The tens column (`10 + 10`)
     */
    override fun getAffectedFactIds(exercise: Exercise): List<String> {
        val (operation, op1, op2) = exercise.equation.getFact()
        val factId = exercise.getFactId()

        val o1 = op1 % 10
        val o2 = op2 % 10
        val t1 = (op1 / 10) * 10
        val t2 = (op2 / 10) * 10

        val onesId = if (operation == Operation.ADDITION) "$o1 + $o2 = ?" else "$o1 - $o2 = ?"
        val tensId = if (operation == Operation.ADDITION) "$t1 + $t2 = ?" else "$t1 - $t2 = ?"

        return listOf(factId, onesId, tensId).distinct()
    }
}
