package com.codinglikeapirate.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class CurriculumTest {

    @Test
    fun `getAllLevels returns all levels in correct order`() {
        val expectedIds = listOf("ADD_SUM_5", "ADD_SUM_10")
        val actualIds = Curriculum.getAllLevels().map { it.id }
        assertEquals("The list of level IDs should match the expected order", expectedIds, actualIds)
    }

    @Test
    fun `SumsUpTo5 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "ADD_SUM_5" }!!
        val factIds = level.getAllPossibleFactIds()

        // Sums up to 5: 1+2+3+4+5+6 = 21 facts
        assertEquals(21, factIds.size)
        assertEquals(true, factIds.contains("ADDITION_0_5"))
        assertEquals(true, factIds.contains("ADDITION_5_0"))
        assertEquals(true, factIds.contains("ADDITION_2_3"))
    }

    @Test
    fun `SumsUpTo10 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "ADD_SUM_10" }!!
        val factIds = level.getAllPossibleFactIds()

        // Sums up to 10: 1+2+3+4+5+6+7+8+9+10+11 = 66 facts
        assertEquals(66, factIds.size)
        assertEquals(true, factIds.contains("ADDITION_0_10"))
        assertEquals(true, factIds.contains("ADDITION_10_0"))
        assertEquals(true, factIds.contains("ADDITION_4_6"))
    }
}
