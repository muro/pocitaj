package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoDigitComputationLevelTest {

    @Test
    fun `level creates correct equation type`() {
        val levelAdd = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val exerciseAdd = levelAdd.createExercise("19 + 19 = ?")
        assertTrue(exerciseAdd.equation is Addition)
        assertTrue(levelAdd.recognizes(exerciseAdd.equation))

        val levelSub = TwoDigitComputationLevel("SUB_BORROW", Operation.SUBTRACTION, true)
        val exerciseSub = levelSub.createExercise("24 - 19 = ?")
        assertTrue(exerciseSub.equation is Subtraction)
        assertTrue(levelSub.recognizes(exerciseSub.equation))
    }

    @Test
    fun `getAffectedFactIds handles multi-fact updates in TwoDigitComputationLevel`() {
        val level = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val exercise = level.createExercise(Addition(19, 19))

        val affectedIds = level.getAffectedFactIds(exercise)

        // Assert main fact and component facts are identified
        assertTrue(affectedIds.contains("19 + 19 = ?"))
        assertTrue(affectedIds.contains("9 + 9 = ?"))
        assertTrue(affectedIds.contains("10 + 10 = ?"))
        assertEquals(3, affectedIds.size)
    }

    @Test
    fun `getAllPossibleFactIds returns component IDs`() {
        val level = TwoDigitComputationLevel("ADD_CARRY", Operation.ADDITION, true)
        val ids = level.getAllPossibleFactIds()

        // Should contain component facts like "9 + 9 = ?" and "10 + 10 = ?"
        assertTrue(ids.contains("9 + 9 = ?"))
        assertTrue(ids.contains("10 + 10 = ?"))

        // Should NOT contain the composite 19+19 in its primary list
        // (getAllPossibleFactIds returns the underlying skills we track mastery for)
        assertTrue(!ids.contains("19 + 19 = ?"))

        // Size should be manageable (fundamental components only)
        // Ones: ~45 (sums >= 10). Tens: ~36 (sums <= 80). Total < 100.
        assertTrue(ids.size in 50..100)
    }
}
