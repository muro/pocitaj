package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.FactMastery
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseProviderTest {

    @Test
    fun `new user is always given an exercise from the first level`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = emptyMap<String, FactMastery>()

        val provider = ExerciseProvider(curriculum, userMastery)
        val exercise = provider.getNextExercise()

        // To verify the level, we find which level in the curriculum contains this exercise
        val exerciseLevel = curriculum.find { level ->
            level.getAllPossibleFactIds().contains("${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}")
        }

        assertEquals("ADD_SUM_5", exerciseLevel?.id)
    }
}
