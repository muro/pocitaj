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
        // 9 Addition + 6 Subtraction + 13 multiplication (0-12) + 10 division (1-10) + 8 mixed review
        val expectedCount = 9 + 6 + 13 + 10 + 8
        assertEquals(expectedCount, levels.size)
    }

    @Test
    fun `MultiplicationTableLevel generates correct exercises`() {
        val table = 7
        val level = Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        repeat(100) {
            val exercise = level.generateExercise()
            val equation = exercise.equation as Multiplication
            assertTrue("One of the operands must be $table", equation.a == table || equation.b == table)
        }
    }

    @Test
    fun `MultiplicationTableLevel generates correct fact IDs`() {
        val table = 7
        val level = Curriculum.getLevelsFor(Operation.MULTIPLICATION).find { it.id == "MUL_TABLE_$table" }!!
        val factIds = level.getAllPossibleFactIds()

        // (0..10) for op2 -> 11 facts. Times 2 for commutativity, but some are duplicates (e.g., 7x7)
        // The set logic handles duplicates, so we check for specific examples.
        assertEquals(25, factIds.size) // 12 pairs, 7x7 is not duplicated
        assertTrue(factIds.contains("MULTIPLICATION_7_0"))
        assertTrue(factIds.contains("MULTIPLICATION_0_7"))
        assertTrue(factIds.contains("MULTIPLICATION_7_12"))
        assertTrue(factIds.contains("MULTIPLICATION_12_7"))
        assertTrue(factIds.contains("MULTIPLICATION_7_7"))
    }

    @Test
    fun `DivisionTableLevel generates correct exercises`() {
        val divisor = 6
        val level = Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
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
        val level = Curriculum.getLevelsFor(Operation.DIVISION).find { it.id == "DIV_BY_$divisor" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(11, factIds.size) // 0..10 for the result
        assertTrue(factIds.contains("DIVISION_0_6"))
        assertTrue(factIds.contains("DIVISION_30_6"))
        assertTrue(factIds.contains("DIVISION_60_6"))
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
