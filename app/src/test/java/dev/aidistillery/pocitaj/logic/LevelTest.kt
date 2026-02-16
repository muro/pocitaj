package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelTest {

    @Test
    fun `default getAffectedFactIds returns single fact`() {
        val level = Curriculum.SumsUpTo5
        val exercise = Exercise(Addition(2, 3))

        val affected = level.getAffectedFactIds(exercise)

        assertEquals(1, affected.size)
        assertEquals("2 + 3 = ?", affected[0])
    }

    @Test
    fun `calculateProgress handles empty facts`() {
        val level = object : Level {
            override val id = "EMPTY"
            override val operation = Operation.ADDITION
            override val prerequisites = emptySet<String>()
            override val strategy = ExerciseStrategy.DRILL
            override fun generateExercise(): Exercise = throw IllegalStateException()
            override fun getAllPossibleFactIds(): List<String> = emptyList()
        }
        assertEquals(0f, level.calculateProgress(emptyMap()), 0.001f)
    }

    @Test
    fun `calculateProgress uses weighted strengths`() {
        val level = object : Level {
            override val id = "TEST"
            override val operation = Operation.ADDITION
            override val prerequisites = emptySet<String>()
            override val strategy = ExerciseStrategy.DRILL
            override fun generateExercise(): Exercise = throw IllegalStateException()
            override fun getAllPossibleFactIds(): List<String> = listOf("f1", "f2", "f3", "f4")
        }

        val masteryMap = mapOf(
            "f1" to FactMastery("f1", 1, "TEST", 2, 0L), // Familiar: 0.1
            "f2" to FactMastery("f2", 1, "TEST", 4, 0L), // Partial: 0.5
            "f3" to FactMastery("f3", 1, "TEST", 5, 0L)  // Mastered: 1.0
            // f4 is unseen: 0.0
        )

        // (0.1 + 0.5 + 1.0 + 0.0) / 4 = 1.6 / 4 = 0.4
        assertEquals(0.4f, level.calculateProgress(masteryMap), 0.001f)
    }
}
