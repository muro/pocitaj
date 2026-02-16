package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class CurriculumTest {

    @Test
    fun `getAllLevels returns correct number of levels`() {
        val levels = Curriculum.getAllLevels()
        // 10 Addition + 6 Subtraction + 11 multiplication (2-12) + 9 division (2-10) + 8 mixed review
        val expectedCount = 10 + 6 + 11 + 9 + 8
        levels.size shouldBe expectedCount
    }

    @Test
    fun `SumsOver10 generates exercises that cross 10`() {
        val level = Curriculum.SumsOver10
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.ADDITION
            (op1 + op2 > 10).shouldBeTrue()
        }
    }

    @Test
    fun `TwoDigitAdditionNoCarry generates correct exercises`() {
        val level = Curriculum.TwoDigitAdditionNoCarry
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.ADDITION
            ((op1 % 10) + (op2 % 10) < 10).shouldBeTrue()
        }
    }

    @Test
    fun `TwoDigitAdditionWithCarry generates correct exercises`() {
        val level = Curriculum.TwoDigitAdditionWithCarry
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.ADDITION
            ((op1 % 10) + (op2 % 10) >= 10).shouldBeTrue()
            (op1 + op2 < 100).shouldBeTrue()
        }
    }

    @Test
    fun `TwoDigitSubtractionNoBorrow generates correct exercises`() {
        val level = Curriculum.TwoDigitSubtractionNoBorrow
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.SUBTRACTION
            ((op1 % 10) >= (op2 % 10)).shouldBeTrue()
        }
    }

    @Test
    fun `TwoDigitSubtractionWithBorrow generates correct exercises`() {
        val level = Curriculum.TwoDigitSubtractionWithBorrow
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.SUBTRACTION
            ((op1 % 10) < (op2 % 10)).shouldBeTrue()
        }
    }

    @Test
    fun `Making10s generates sums of 10 with missing operands`() {
        val level = Curriculum.Making10s
        var missingOp1Count = 0
        var missingOp2Count = 0

        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation
            val (op, op1, op2) = equation.getFact()

            op shouldBe Operation.ADDITION
            (op1 + op2) shouldBe 10

            equation.shouldBeInstanceOf<MissingAddend>()
            if (equation.a == null) missingOp1Count++
            if (equation.b == null) missingOp2Count++
        }

        (missingOp1Count > 0).shouldBeTrue()
        (missingOp2Count > 0).shouldBeTrue()
    }

    @Test
    fun `Making10s generates correct fact IDs`() {
        val level = Curriculum.Making10s
        val factIds = level.getAllPossibleFactIds()

        // Should contain both "3 + ? = 10" and "? + 3 = 10"
        factIds shouldContain "3 + ? = 10"
        factIds shouldContain "? + 3 = 10"
    }

    @Test
    fun `Doubles generates exercises with identical operands`() {
        val level = Curriculum.Doubles
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            op shouldBe Operation.ADDITION
            op1 shouldBe op2
        }
    }

    @Test
    fun `MultiplicationTableLevel generates correct exercises`() {
        val table = 7
        val level =
            Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Multiplication
            (equation.a == table || equation.b == table).shouldBeTrue()
        }
    }

    @Test
    fun `MultiplicationTableLevel generates correct fact IDs`() {
        val table = 7
        val level =
            Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        val factIds = level.getAllPossibleFactIds()

        // The set logic handles duplicates, so we check for specific examples.
        factIds.size shouldBe 21 // 11 pairs, 7x7 is not duplicated
        factIds shouldContain "7 * 2 = ?"
        factIds shouldContain "2 * 7 = ?"
        factIds shouldContain "7 * 12 = ?"
        factIds shouldContain "12 * 7 = ?"
        factIds shouldContain "7 * 7 = ?"
    }

    @Test
    fun `DivisionTableLevel generates correct exercises`() {
        val divisor = 6
        val level =
            Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Division
            equation.b shouldBe divisor
            (equation.a % divisor == 0).shouldBeTrue()
        }
    }

    @Test
    fun `DivisionTableLevel generates correct fact IDs`() {
        val divisor = 6
        val level =
            Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        val factIds = level.getAllPossibleFactIds()

        factIds.size shouldBe 9 // 2..10 for the result
        factIds shouldContain "12 / 6 = ?"
        factIds shouldContain "30 / 6 = ?"
        factIds shouldContain "60 / 6 = ?"
    }

    @Test
    fun `MixedReviewLevel combines facts and prerequisites correctly`() {
        // ARRANGE: Use existing levels from the Curriculum
        val level1 = Curriculum.SumsUpTo5
        val level2 = Curriculum.SumsUpTo10
        val mixedLevel = MixedReviewLevel("MIXED_TEST", Operation.ADDITION, listOf(level1, level2))

        // ACT & ASSERT

        // 1. Check prerequisites
        val expectedPrerequisites = setOf(level1.id, level2.id)
        mixedLevel.prerequisites shouldBe expectedPrerequisites

        // 2. Check combined fact IDs
        val expectedFactIds = level1.getAllPossibleFactIds() + level2.getAllPossibleFactIds()
        mixedLevel.getAllPossibleFactIds().toSet() shouldBe expectedFactIds.toSet()

        // 3. Check exercise generation to ensure both levels are represented
        val generatedLevelIds = mutableSetOf<String>()
        repeat(200) { // Generate enough exercises to reasonably expect a mix
            val exercise = mixedLevel.generateExercise()
            val level = Curriculum.getLevelForExercise(exercise)
            level.shouldNotBeNull()
            generatedLevelIds.add(level.id)
        }
        generatedLevelIds shouldBe expectedPrerequisites
    }
}
