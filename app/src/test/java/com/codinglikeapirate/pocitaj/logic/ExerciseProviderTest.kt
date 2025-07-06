package com.codinglikeapirate.pocitaj.logic

import com.codinglikeapirate.pocitaj.data.FactMastery
import org.junit.Assert.assertEquals
import org.junit.Test

import kotlin.random.Random

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

    @Test
    fun `provider picks the weakest fact from the current level`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 4, 0),
            "ADDITION_1_2" to FactMastery("ADDITION_1_2", 1, 2, 0), // Weakest
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, 5, 0)
        )

        val provider = ExerciseProvider(curriculum, userMastery)
        val exercise = provider.getNextExercise()

        assertEquals(1, exercise.operand1)
        assertEquals(2, exercise.operand2)
    }

    @Test
    fun `provider uses lastTestedTimestamp as a tie-breaker`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 2, 1000L),
            "ADDITION_1_2" to FactMastery("ADDITION_1_2", 1, 2, 500L), // Weakest (same strength, older timestamp)
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, 3, 1500L)
        )

        val provider = ExerciseProvider(curriculum, userMastery)
        val exercise = provider.getNextExercise()

        assertEquals(1, exercise.operand1)
        assertEquals(2, exercise.operand2)
    }

    @Test
    fun `provider moves to the next level after mastery`() {
        val curriculum = Curriculum.getAllLevels()
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        val userMastery = level1Facts.associateWith { factId ->
            FactMastery(factId, 1, 5, 0) // Strength 5 = mastered
        }

        val provider = ExerciseProvider(curriculum, userMastery)
        val level = provider.findCurrentLevel()
        assertEquals("ADD_SUM_10", level.id)
    }

    @Test
    fun `provider selects a review question from a mastered level`() {
        val curriculum = Curriculum.getAllLevels()
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        val userMastery = level1Facts.associateWith { factId ->
            FactMastery(factId, 1, 5, 0) // Mastered
        }

        // By providing a Random generator that always returns a high value,
        // we force the provider to choose a review question.
        val controlledRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float = 0.9f // This will trigger the 'else' branch for review
        }

        val provider = ExerciseProvider(curriculum, userMastery, random = controlledRandom)
        val exercise = provider.getNextExercise()

        val exerciseLevel = curriculum.find { level ->
            level.getAllPossibleFactIds().contains("${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}")
        }
        assertEquals("ADD_SUM_5", exerciseLevel?.id) // Should be from the mastered level
    }
}
