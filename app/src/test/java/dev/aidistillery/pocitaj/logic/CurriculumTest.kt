package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `getAllLevels returns correct number of levels`() {
        val levels = Curriculum.getAllLevels()
        // 10 Addition + 6 Subtraction + 11 multiplication (2-12) + 9 division (2-10) + 8 mixed review
        val expectedCount = 10 + 6 + 11 + 9 + 8
        assertEquals(expectedCount, levels.size)
    }

    @Test
    fun `SumsOver10 generates exercises that cross 10`() {
        val level = Curriculum.SumsOver10
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be addition", Operation.ADDITION, op)
            assertTrue("Sum must be > 10 for $op1 + $op2", op1 + op2 > 10)
        }
    }

    @Test
    fun `TwoDigitAdditionNoCarry generates correct exercises`() {
        val level = Curriculum.TwoDigitAdditionNoCarry
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be addition", Operation.ADDITION, op)
            assertTrue(
                "Sum of units must be < 10 for $op1 + $op2",
                (op1 % 10) + (op2 % 10) < 10
            )
        }
    }

    @Test
    fun `TwoDigitAdditionWithCarry generates correct exercises`() {
        val level = Curriculum.TwoDigitAdditionWithCarry
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be addition", Operation.ADDITION, op)
            assertTrue(
                "Sum of units must be >= 10 for $op1 + $op2",
                (op1 % 10) + (op2 % 10) >= 10
            )
            assertTrue("Total sum must be < 100 for $op1 + $op2", op1 + op2 < 100)
        }
    }

    @Test
    fun `TwoDigitSubtractionNoBorrow generates correct exercises`() {
        val level = Curriculum.TwoDigitSubtractionNoBorrow
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be subtraction", Operation.SUBTRACTION, op)
            assertTrue(
                "op1 unit must be >= op2 unit for $op1 - $op2",
                (op1 % 10) >= (op2 % 10)
            )
        }
    }

    @Test
    fun `TwoDigitSubtractionWithBorrow generates correct exercises`() {
        val level = Curriculum.TwoDigitSubtractionWithBorrow
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be subtraction", Operation.SUBTRACTION, op)
            assertTrue(
                "op1 unit must be < op2 unit for $op1 - $op2",
                (op1 % 10) < (op2 % 10)
            )
        }
    }

    @Test
    fun `Making10s generates sums of 10`() {
        val level = Curriculum.Making10s
        repeat(20) { // Only a few pairs exist, 20 is plenty
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be addition", Operation.ADDITION, op)
            assertEquals("Sum must be 10 for $op1 + $op2", 10, op1 + op2)
        }
    }

    @Test
    fun `Doubles generates exercises with identical operands`() {
        val level = Curriculum.Doubles
        repeat(100) {
            val exercise = level.generateExercise()
            val (op, op1, op2) = exercise.equation.getFact()
            assertEquals("Operation must be addition", Operation.ADDITION, op)
            assertEquals("Operands must be equal for $op1 + $op2", op1, op2)
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
            assertTrue(
                "One of the operands must be $table",
                equation.a == table || equation.b == table
            )
        }
    }

    @Test
    fun `MultiplicationTableLevel generates correct fact IDs`() {
        val table = 7
        val level =
            Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        val factIds = level.getAllPossibleFactIds()

        // The set logic handles duplicates, so we check for specific examples.
        assertEquals(21, factIds.size) // 11 pairs, 7x7 is not duplicated
        assertTrue(factIds.contains("7 * 2 = ?"))
        assertTrue(factIds.contains("2 * 7 = ?"))
        assertTrue(factIds.contains("7 * 12 = ?"))
        assertTrue(factIds.contains("12 * 7 = ?"))
        assertTrue(factIds.contains("7 * 7 = ?"))
    }

    @Test
    fun `DivisionTableLevel generates correct exercises`() {
        val divisor = 6
        val level =
            Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Division
            assertEquals("The divisor must be $divisor", divisor, equation.b)
            assertTrue("The dividend must be divisible by the divisor", equation.a % divisor == 0)
        }
    }

    @Test
    fun `DivisionTableLevel generates correct fact IDs`() {
        val divisor = 6
        val level =
            Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(9, factIds.size) // 2..10 for the result
        assertTrue(factIds.contains("12 / 6 = ?"))
        assertTrue(factIds.contains("30 / 6 = ?"))
        assertTrue(factIds.contains("60 / 6 = ?"))
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
        assertEquals(expectedPrerequisites, mixedLevel.prerequisites)

        // 2. Check combined fact IDs
        val expectedFactIds = level1.getAllPossibleFactIds() + level2.getAllPossibleFactIds()
        assertEquals(expectedFactIds.toSet(), mixedLevel.getAllPossibleFactIds().toSet())

        // 3. Check exercise generation to ensure both levels are represented
        val generatedLevelIds = mutableSetOf<String>()
        repeat(200) { // Generate enough exercises to reasonably expect a mix
            val exercise = mixedLevel.generateExercise()
            val level = Curriculum.getLevelForExercise(exercise)
            assertNotNull("Generated exercise must belong to a level", level)
            generatedLevelIds.add(level!!.id)
        }
        assertEquals(
            "Generated exercises should come from both source levels",
            expectedPrerequisites,
            generatedLevelIds
        )
    }
}
