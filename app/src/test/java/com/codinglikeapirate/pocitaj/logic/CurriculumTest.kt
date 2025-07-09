package com.codinglikeapirate.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {

    @Test
    fun `getAllLevels returns all levels in correct order`() {
        val expectedIds = listOf(
            "ADD_SUM_5",
            "ADD_SUM_10",
            "SUB_FROM_5",
            "MUL_TABLES_0_1_2_5_10",
            "DIV_BY_2_5_10"
        )
        val actualIds = Curriculum.getAllLevels().map { it.id }
        assertEquals("The list of level IDs should match the expected order", expectedIds, actualIds)
    }

    @Test
    fun `SumsUpTo5 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "ADD_SUM_5" }!!
        val factIds = level.getAllPossibleFactIds()

        // Sums up to 5: 1+2+3+4+5+6 = 21 facts
        assertEquals(21, factIds.size)
        assertTrue(factIds.contains("ADDITION_0_5"))
        assertTrue(factIds.contains("ADDITION_5_0"))
        assertTrue(factIds.contains("ADDITION_2_3"))
    }

    @Test
    fun `SumsUpTo10 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "ADD_SUM_10" }!!
        val factIds = level.getAllPossibleFactIds()

        // Sums up to 10: 1+2+3+4+5+6+7+8+9+10+11 = 66 facts
        assertEquals(66, factIds.size)
        assertTrue(factIds.contains("ADDITION_0_10"))
        assertTrue(factIds.contains("ADDITION_10_0"))
        assertTrue(factIds.contains("ADDITION_4_6"))
    }

    @Test
    fun `SubtractionFrom5 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "SUB_FROM_5" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(21, factIds.size)
        assertTrue(factIds.contains("SUBTRACTION_5_0"))
        assertTrue(factIds.contains("SUBTRACTION_5_5"))
        assertTrue(factIds.contains("SUBTRACTION_3_2"))
    }

    @Test
    fun `MultiplicationTables012510 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "MUL_TABLES_0_1_2_5_10" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(5 * 11, factIds.size)
        assertTrue(factIds.contains("MULTIPLICATION_0_10"))
        assertTrue(factIds.contains("MULTIPLICATION_5_5"))
        assertTrue(factIds.contains("MULTIPLICATION_2_8"))
    }

    @Test
    fun `DivisionBy2510 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "DIV_BY_2_5_10" }!!
        val factIds = level.getAllPossibleFactIds()

        assertEquals(3 * 11, factIds.size)
        assertTrue(factIds.contains("DIVISION_20_2"))
        assertTrue(factIds.contains("DIVISION_50_5"))
        assertTrue(factIds.contains("DIVISION_100_10"))
    }
}
