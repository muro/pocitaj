package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import kotlin.random.Random

/**
 * A singleton object that holds the entire curriculum for the app.
 */
object Curriculum {

    // --- Level Definitions ---

    // --- Addition ---
    object SumsUpTo5 : Level {
        override val id = "ADD_SUM_5"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = emptySet()
        override val strategy = ExerciseStrategy.DRILL
        private const val max_sum = 5

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    object SumsUpTo10 : Level {
        override val id = "ADD_SUM_10"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo5.id)
        override val strategy = ExerciseStrategy.DRILL
        private const val max_sum = 10

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    object SumsOver10 : Level {
        override val id = "ADD_SUM_OVER_10"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo10.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            // e.g. 9+x, 8+x, 7+x, 6+x
            val op1 = Random.nextInt(6, 10)
            // We want the sum to cross 10
            val op2 = Random.nextInt(10 - op1 + 1, 10)
            return if (Random.nextBoolean()) {
                Exercise(Addition(op1, op2))
            } else {
                Exercise(Addition(op2, op1))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (6..9).flatMap { op1 ->
                (10 - op1 + 1 until 10).flatMap { op2 ->
                    listOf(
                        "${operation.name}_${op1}_${op2}",
                        "${operation.name}_${op2}_${op1}"
                    )
                }
            }
        }
    }

    object SumsUpTo20 : Level {
        override val id = "ADD_SUM_20"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo10.id)
        override val strategy = ExerciseStrategy.DRILL
        private const val max_sum = 20

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(0, max_sum + 1)
            val op2 = Random.nextInt(0, max_sum - op1 + 1)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (0..max_sum).flatMap { op1 ->
                (0..max_sum - op1).map { op2 ->
                    "${operation.name}_${op1}_${op2}"
                }
            }
        }
    }

    object Doubles : Level {
        override val id = "ADD_DOUBLES"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(SumsUpTo10.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(1, 11)
            return Exercise(Addition(op1, op1))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..10).map { "${operation.name}_${it}_${it}" }
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
                Exercise(Addition(op1, op2))
            } else {
                Exercise(Addition(op2, op1))
            }
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).flatMap { op1 ->
                val op2 = op1 + 1
                listOf(
                    "${operation.name}_${op1}_${op2}",
                    "${operation.name}_${op2}_${op1}"
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
            val exercises = (1..9).map { it to (10 - it) }
            val (op1, op2) = exercises.random()
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).mapNotNull { op1 ->
                val op2 = 10 - op1
                if (op2 > 0) "${operation.name}_${op1}_${op2}" else null
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
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (1..9).flatMap { op1 ->
                (1..9).map { op2 ->
                    "${operation.name}_${op1 * 10}_${op2 * 10}"
                }
            }
        }
    }

    object TwoDigitAdditionNoCarry : Level {
        override val id = "ADD_TWO_DIGIT_NO_CARRY"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(AddingTens.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(10, 100)
                op2 = Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) >= 10 || op1 + op2 >= 100)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (10..99).flatMap { op1 ->
                (10..99).mapNotNull { op2 ->
                    if ((op1 % 10) + (op2 % 10) < 10 && op1 + op2 < 100) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    object TwoDigitAdditionWithCarry : Level {
        override val id = "ADD_TWO_DIGIT_CARRY"
        override val operation = Operation.ADDITION
        override val prerequisites: Set<String> = setOf(TwoDigitAdditionNoCarry.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(10, 100)
                op2 = Random.nextInt(10, 100)
            } while ((op1 % 10) + (op2 % 10) < 10 || op1 + op2 >= 100)
            return Exercise(Addition(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (10..99).flatMap { op1 ->
                (10..99).mapNotNull { op2 ->
                    if ((op1 % 10) + (op2 % 10) >= 10 && op1 + op2 < 100) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    // --- Subtraction ---
    object SubtractionFrom5 : Level {
        override val id = "SUB_FROM_5"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = emptySet()
        override val strategy = ExerciseStrategy.DRILL
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

    object SubtractionFrom10 : Level {
        override val id = "SUB_FROM_10"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(SubtractionFrom5.id)
        override val strategy = ExerciseStrategy.DRILL
        private const val max_minuend = 10

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

    object SubtractionFrom20 : Level {
        override val id = "SUB_FROM_20"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(SubtractionFrom10.id)
        override val strategy = ExerciseStrategy.DRILL
        private const val max_minuend = 20

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

    object SubtractingTens : Level {
        override val id = "SUB_TENS"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(SubtractionFrom20.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val op1 = Random.nextInt(2, 10) * 10
            val op2 = Random.nextInt(1, op1 / 10) * 10
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (2..9).flatMap { op1Tens ->
                (1 until op1Tens).map { op2Tens ->
                    "${operation.name}_${op1Tens * 10}_${op2Tens * 10}"
                }
            }
        }
    }

    object TwoDigitSubtractionNoBorrow : Level {
        override val id = "SUB_TWO_DIGIT_NO_BORROW"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(SubtractingTens.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(11, 100)
                op2 = Random.nextInt(10, op1)
            } while ((op1 % 10) < (op2 % 10))
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (11..99).flatMap { op1 ->
                (10 until op1).mapNotNull { op2 ->
                    if ((op1 % 10) >= (op2 % 10)) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }

    object TwoDigitSubtractionWithBorrow : Level {
        override val id = "SUB_TWO_DIGIT_BORROW"
        override val operation = Operation.SUBTRACTION
        override val prerequisites: Set<String> = setOf(TwoDigitSubtractionNoBorrow.id)
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            var op1: Int
            var op2: Int
            do {
                op1 = Random.nextInt(11, 100)
                op2 = Random.nextInt(10, op1)
            } while ((op1 % 10) >= (op2 % 10))
            return Exercise(Subtraction(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (11..99).flatMap { op1 ->
                (10 until op1).mapNotNull { op2 ->
                    if ((op1 % 10) < (op2 % 10)) {
                        "${operation.name}_${op1}_${op2}"
                    } else {
                        null
                    }
                }
            }
        }
    }


    // --- Multiplication ---
    class MultiplicationTableLevel(val table: Int) : Level {
        override val id = "MUL_TABLE_$table"
        override val operation = Operation.MULTIPLICATION
        override val prerequisites: Set<String> = emptySet()
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            var op1 = table
            var op2 = Random.nextInt(2, 13)
            if (Random.nextBoolean()) {
                val temp = op1
                op1 = op2
                op2 = temp
            }
            return Exercise(Multiplication(op1, op2))
        }

        override fun getAllPossibleFactIds(): List<String> {
            val facts = mutableSetOf<String>()
            (2..12).forEach { op2 ->
                facts.add("${operation.name}_${table}_${op2}")
                facts.add("${operation.name}_${op2}_${table}")
            }
            return facts.toList()
        }
    }

    // --- Division ---
    class DivisionTableLevel(val divisor: Int) : Level {
        override val id = "DIV_BY_$divisor"
        override val operation = Operation.DIVISION
        override val prerequisites: Set<String> = emptySet()
        override val strategy = ExerciseStrategy.DRILL

        override fun generateExercise(): Exercise {
            val result = Random.nextInt(2, 11)
            val op1 = divisor * result
            return Exercise(Division(op1, divisor))
        }

        override fun getAllPossibleFactIds(): List<String> {
            return (2..10).map { result ->
                val op1 = divisor * result
                "${operation.name}_${op1}_${divisor}"
            }
        }
    }

    // --- Public API ---

    fun getAllLevels(): List<Level> {
        val additionLevels = listOf(
            SumsUpTo5,
            SumsUpTo10,
            SumsOver10,
            SumsUpTo20,
            Doubles,
            NearDoubles,
            Making10s,
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

        val multiplicationLevels = (2..12).map { MultiplicationTableLevel(it) }
        val divisionLevels = (2..10).map { DivisionTableLevel(it) }

        val mulTableMap = multiplicationLevels.associateBy { it.table }
        val divTableMap = divisionLevels.associateBy { it.divisor }

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
