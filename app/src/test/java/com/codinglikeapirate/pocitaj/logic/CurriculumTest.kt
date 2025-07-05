package com.codinglikeapirate.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class CurriculumTest {

    @Test
    fun `SumsUpTo5 level generates correct fact IDs`() {
        val level = Curriculum.getAllLevels().find { it.id == "ADD_SUM_5" }!!
        val factIds = level.getAllPossibleFactIds()

        // Total number of unique facts for sums up to 5 (including 0+0, 0+1, 1+0, etc.)
        // 0: 1 (0+0)
        // 1: 2 (0+1, 1+0)
        // 2: 3 (0+2, 1+1, 2+0)
        // 3: 4 (0+3, 1+2, 2+1, 3+0)
        // 4: 5 (0+4, 1+3, 2+2, 3+1, 4+0)
        // 5: 6 (0+5, 1+4, 2+3, 3+2, 4+1, 5+0)
        // Total = 1+2+3+4+5+6 = 21
        assertEquals(21, factIds.size)
        assertEquals(true, factIds.contains("ADDITION_0_5"))
        assertEquals(true, factIds.contains("ADDITION_5_0"))
        assertEquals(true, factIds.contains("ADDITION_2_3"))
    }
}
