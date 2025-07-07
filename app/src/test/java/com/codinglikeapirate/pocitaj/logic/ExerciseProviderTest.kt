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

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        assertEquals("ADD_SUM_5", exerciseLevel?.id)
    }

    @Test
    fun `provider picks from weakest facts`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 4, 0),
            "ADDITION_1_2" to FactMastery("ADDITION_1_2", 1, 2, 0), // Weakest
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, 5, 0)
        )

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val exercise = provider.getNextExercise()

        // The working set should contain the weakest fact, plus new facts
        val exerciseId = "${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}"
        assert(exerciseId == "ADDITION_1_2" || !userMastery.containsKey(exerciseId))
    }

    @Test
    fun `provider uses lastTestedTimestamp as a tie-breaker`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, 2, 1000L),
            "ADDITION_1_2" to FactMastery("ADDITION_1_2", 1, 2, 500L), // Weakest (same strength, older timestamp)
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, 3, 1500L)
        )

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseId = "${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}"
        assert(exerciseId == "ADDITION_1_2" || !userMastery.containsKey(exerciseId))
    }

    @Test
    fun `provider moves to the next level after mastery`() {
        val curriculum = Curriculum.getAllLevels()
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        val userMastery = level1Facts.associateWith { factId ->
            FactMastery(factId, 1, 5, 0) // Strength 5 = mastered
        }

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        assertEquals("ADD_SUM_10", exerciseLevel?.id)
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

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        assertEquals("ADD_SUM_5", exerciseLevel?.id) // Should be from the mastered level
    }

    @Test
    fun `initial working set is created for new level`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = emptyMap<String, FactMastery>()

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(10) {
            val exercise = provider.getNextExercise()
            workingSet.add("${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}")
        }

        assertEquals(5, workingSet.size)
    }

    @Test
    fun `working set is limited to weakest facts`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf<String, FactMastery>()
        // Create 7 unmastered facts
        for (i in 0..6) {
            userMastery["ADDITION_1_$i"] = FactMastery("ADDITION_1_$i", 1, i + 1, 0)
        }

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(20) { // Repeat enough times to likely get all facts from the working set
            val exercise = provider.getNextExercise()
            workingSet.add("${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}")
        }

        assertEquals(5, workingSet.size)
        // Check that the working set contains only weak facts
        for (factId in workingSet) {
            val strength = userMastery[factId]?.strength ?: 0
            assert(strength <= 5)
        }
    }

    @Test
    fun `mastered fact is replaced by new fact`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf<String, FactMastery>()
        // Create 4 unmastered facts
        for (i in 0..3) {
            userMastery["ADDITION_1_$i"] = FactMastery("ADDITION_1_$i", 1, 1, 0)
        }
        // Create 1 mastered fact
        userMastery["ADDITION_1_4"] = FactMastery("ADDITION_1_4", 1, 5, 0)

        val provider = ExerciseProvider(curriculum, userMastery, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(20) {
            val exercise = provider.getNextExercise()
            workingSet.add("${exercise.operation.name}_${exercise.operand1}_${exercise.operand2}")
        }

        assertEquals(5, workingSet.size)
        // Check that the mastered fact is not in the working set
        assert(!workingSet.contains("ADDITION_1_4"))
    }
}
