package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import kotlin.random.Random

/**
 * A singleton object that holds the entire curriculum for the app.
 */
object Curriculum {

    // --- Level Definitions ---
    class RangeLevel(
        override val id: String,
        override val operation: Operation,
        private val minRange: Int,
        private val maxRange: Int,
        override val prerequisites: Set<String> = emptySet(),
        override val strategy: ExerciseStrategy = ExerciseStrategy.DRILL
    ) : Level {

        override fun generateExercise(): Exercise {
            // For ADDITION: minRange..maxRange is the target sum.
            // For SUBTRACTION: minRange..maxRange is the minuend (op1).
            // Mathematically, generating 'whole' in [min, max] and 'part' in [0, whole].
            val whole = Random.nextInt(minRange, maxRange + 1)
            val part = Random.nextInt(0, whole + 1)

            return if (operation == Operation.ADDITION) {
                createExercise(Addition(part, whole - part))
            } else {
                createExercise(Subtraction(whole, part))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (minRange..maxRange).flatMap { whole ->
                (0..whole).map { part ->
                    if (operation == Operation.ADDITION) {
                        "$part + ${whole - part} = ?"
                    } else {
                        "$whole - $part = ?"
                    }
                }
            }
        }
    }

    // --- Multiplication & Division Table Level ---
    class TableLevel(
        override val operation: Operation,
        private val number: Int
    ) : Level {
        // ID format: MUL_TABLE_X or DIV_BY_X
        override val id: String = if (operation == Operation.MULTIPLICATION) {
            "MUL_TABLE_$number"
        } else {
            "DIV_BY_$number"
        }

        override val prerequisites: Set<String> = emptySet()
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            return if (operation == Operation.MULTIPLICATION) {
                var op1 = number
                var op2 = Random.nextInt(2, 13)
                // 50% chance to swap operands for commutativity
                if (Random.nextBoolean()) {
                    val temp = op1
                    op1 = op2
                    op2 = temp
                }
                createExercise(Multiplication(op1, op2))
            } else {
                // Division
                val result = Random.nextInt(2, 11)
                val op1 = number * result
                createExercise(Division(op1, number))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return if (operation == Operation.MULTIPLICATION) {
                val facts = mutableSetOf<String>()
                (2..12).forEach { op2 ->
                    facts.add("$number * $op2 = ?")
                    facts.add("$op2 * $number = ?")
                }
                facts.toList()
            } else {
                (2..10).map { result ->
                    val op1 = number * result
                    "$op1 / $number = ?"
                }
            }
        }
    }

    // --- Addition ---
    val SumsUpTo5 = RangeLevel(
        id = "ADD_SUM_5",
        operation = Operation.ADDITION,
        minRange = 0,
        maxRange = 5
    )


    val SumsUpTo10 = RangeLevel(
        id = "ADD_SUM_10",
        operation = Operation.ADDITION,
        minRange = 6,
        maxRange = 10,
        prerequisites = setOf(SumsUpTo5.id)
    )


    object SumsOver10 : Level {
        override val id = "ADD_SUM_OVER_10"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(Making10s.id, NearDoubles.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            // e.g. 9+x, 8+x, 7+x, 6+x
            val op1 = Random.nextInt(6, 10)
            // We want the sum to cross 10
            val op2 = Random.nextInt(10 - op1 + 1, 10)
            return if (Random.nextBoolean()) {
                createExercise(Addition(op1, op2))
            } else {
                createExercise(Addition(op2, op1))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (6..9).flatMap { op1 ->
                (10 - op1 + 1 until 10).flatMap { op2 ->
                    listOf(
                        "$op1 + $op2 = ?",
                        "$op2 + $op1 = ?"
                    )
                }
            }.distinct() // we add both a+b and b+a, which for doubles is the same
        }
    }

    val SumsUpTo20 = RangeLevel(
        id = "ADD_SUM_20",
        operation = Operation.ADDITION,
        minRange = 11,
        maxRange = 20,
        prerequisites = setOf(SumsOver10.id)
    )


    object Doubles : Level {
        override val id = "ADD_DOUBLES"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo10.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(1, 11)
            return createExercise(Addition(op1, op1))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..10).map { "$it + $it = ?" }
        }
    }

    object NearDoubles : Level {
        override val id = "ADD_NEAR_DOUBLES"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(Doubles.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(1, 10) // e.g. 4
            val op2 = op1 + 1 // e.g. 5
            return if (Random.nextBoolean()) {
                createExercise(Addition(op1, op2))
            } else {
                createExercise(Addition(op2, op1))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).flatMap { op1 ->
                val op2 = op1 + 1
                listOf(
                    "$op1 + $op2 = ?",
                    "$op2 + $op1 = ?"
                )
            }
        }
    }

    object Making10s : Level {
        override val id = "ADD_MAKING_10S"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo10.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            // Making 10s: 3 + ? = 10
            val a = Random.nextInt(1, 10)
            return createExercise(MissingAddend(a, 10))
        }

        override fun getAllPossibleFactIds(): List<String> {
            // IDs: 1 + ? = 10, etc.
            return (1..9).map { a ->
                "$a + ? = 10"
            }
        }
    }

    object AddingTens : Level {
        override val id = "ADD_TENS"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo20.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(1, 10) * 10
            val op2 = Random.nextInt(1, 10) * 10
            return createExercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).flatMap { op1 ->
                (1..9).map { op2 ->
                    "${op1 * 10} + ${op2 * 10} = ?"
                }
            }
        }
    }

    object TwoDigitAdditionNoCarry : TwoDigitComputationLevel(
        "ADD_TWO_DIGIT_NO_CARRY",
        Operation.ADDITION,
        withRegrouping = false
    )

    object TwoDigitAdditionWithCarry :
        TwoDigitComputationLevel("ADD_TWO_DIGIT_CARRY", Operation.ADDITION, withRegrouping = true)

    // --- Subtraction ---

    val SubtractionFrom5 = RangeLevel(
        id = "SUB_FROM_5",
        operation = Operation.SUBTRACTION,
        minRange = 0,
        maxRange = 5
    )

    val SubtractionFrom10 = RangeLevel(
        id = "SUB_FROM_10",
        operation = Operation.SUBTRACTION,
        minRange = 6,
        maxRange = 10,
        prerequisites = setOf(SubtractionFrom5.id)
    )

    val SubtractionFrom20 = RangeLevel(
        id = "SUB_FROM_20",
        operation = Operation.SUBTRACTION,
        minRange = 11,
        maxRange = 20,
        prerequisites = setOf(SubtractionFrom10.id)
    )

    object SubtractingTens : Level {
        override val id = "SUB_TENS"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(SubtractionFrom20.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(2, 10) * 10
            val op2 = Random.nextInt(1, op1 / 10) * 10
            return createExercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (2..9).flatMap { op1Tens ->
                (1 until op1Tens).map { op2Tens ->
                    "${op1Tens * 10} - ${op2Tens * 10} = ?"
                }
            }
        }
    }

    object TwoDigitSubtractionNoBorrow :
        TwoDigitComputationLevel(
            "SUB_TWO_DIGIT_NO_BORROW",
            Operation.SUBTRACTION,
            withRegrouping = false
        )

    object TwoDigitSubtractionWithBorrow :
        TwoDigitComputationLevel(
            "SUB_TWO_DIGIT_BORROW",
            Operation.SUBTRACTION,
            withRegrouping = true
        )

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        val additionLevels = listOf(
            SumsUpTo5,
            SumsUpTo10,
            Doubles,
            NearDoubles,
            Making10s,
            SumsOver10,
            SumsUpTo20,
            AddingTens,
            TwoDigitAdditionNoCarry,
            TwoDigitAdditionWithCarry
        )

        val subtractionLevels = listOf(
            SubtractionFrom5,
            SubtractionFrom10,
            SubtractionFrom20,
            SubtractingTens,
            TwoDigitSubtractionNoBorrow,
            TwoDigitSubtractionWithBorrow
        )

        val multiplicationLevels = (2..12).map { TableLevel(Operation.MULTIPLICATION, it) }
        val divisionLevels = (2..10).map { TableLevel(Operation.DIVISION, it) }

        val mulTableMap =
            multiplicationLevels.associateBy { (it.id.removePrefix("MUL_TABLE_")).toInt() }
        val divTableMap = divisionLevels.associateBy { (it.id.removePrefix("DIV_BY_")).toInt() }

        val mixedReviewLevels = listOf(
            MixedReviewLevel(
                "ADD_REVIEW_1",
                Operation.ADDITION,
                listOf(SumsUpTo5, SumsUpTo10)
            ),
            MixedReviewLevel(
                "SUB_REVIEW_1",
                Operation.SUBTRACTION,
                listOf(SubtractionFrom5, SubtractionFrom10)
            ),
            // Multiplication Reviews
            MixedReviewLevel(
                "MUL_REVIEW_2_5_10",
                Operation.MULTIPLICATION,
                listOfNotNull(mulTableMap[2], mulTableMap[5], mulTableMap[10])
            ),
            MixedReviewLevel(
                "MUL_REVIEW_2_4_8",
                Operation.MULTIPLICATION,
                listOfNotNull(mulTableMap[2], mulTableMap[4], mulTableMap[8])
            ),
            MixedReviewLevel(
                "MUL_REVIEW_2_3_6_9",
                Operation.MULTIPLICATION,
                listOfNotNull(mulTableMap[2], mulTableMap[3], mulTableMap[6], mulTableMap[9])
            ),
            // Division Reviews
            MixedReviewLevel(
                "DIV_REVIEW_2_5_10",
                Operation.DIVISION,
                listOfNotNull(divTableMap[2], divTableMap[5], divTableMap[10])
            ),
            MixedReviewLevel(
                "DIV_REVIEW_2_4_8",
                Operation.DIVISION,
                listOfNotNull(divTableMap[2], divTableMap[4], divTableMap[8])
            ),
            MixedReviewLevel(
                "DIV_REVIEW_2_3_6_9",
                Operation.DIVISION,
                listOfNotNull(divTableMap[2], divTableMap[3], divTableMap[6], divTableMap[9])
            )
        )

        return additionLevels + subtractionLevels + multiplicationLevels + divisionLevels + mixedReviewLevels
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
