package dev.aidistillery.pocitaj.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelTest {

    @Test
    fun `default getAffectedFactIds returns single fact`() {
        val level = Curriculum.SumsUpTo5
        val exercise = level.createExercise(Addition(2, 3))

        val affected = level.getAffectedFactIds(exercise)

        assertEquals(1, affected.size)
        assertEquals("2 + 3 = ?", affected[0])
    }
}
