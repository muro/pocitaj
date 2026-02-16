package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import kotlin.random.Random

class ExerciseProviderTest {

    @Test
    fun `new user is always given an exercise from the first level`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = emptyMap<String, FactMastery>().toMutableMap()

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe "ADD_SUM_5"
    }

    @Test
    fun `provider picks from weakest facts`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, "", 3, 0),
            "ADDITION_1_2" to FactMastery("ADDITION_1_2", 1, "", 1, 0), // Weakest
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, "", 4, 0)
        )

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        // The working set should contain the weakest fact, plus new facts
        val exerciseId = exercise.getFactId()
        (exerciseId == "ADDITION_1_2" || !userMastery.containsKey(exerciseId)).shouldBeTrue()
    }

    @Test
    fun `provider uses lastTestedTimestamp as a tie-breaker`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf(
            "ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, "", 1, 1000L),
            "ADDITION_1_2" to FactMastery(
                "ADDITION_1_2",
                1,
                "",
                1,
                500L
            ), // Weakest (same strength, older timestamp)
            "ADDITION_1_3" to FactMastery("ADDITION_1_3", 1, "", 2, 1500L)
        )

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseId = exercise.getFactId()
        (exerciseId == "ADDITION_1_2" || !userMastery.containsKey(exerciseId)).shouldBeTrue()
    }

    @Test
    fun `provider moves to the next level after mastery`() {
        val curriculum = Curriculum.getAllLevels()
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        val userMastery = level1Facts.associateWith { factId ->
            FactMastery(factId, 1, "", 5, 0) // Strength 5 = mastered
        }.toMutableMap()

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe "ADD_SUM_10"
    }

    @Test
    fun `provider selects a review question from a mastered level`() {
        val curriculum = Curriculum.getAllLevels()
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        val userMastery = level1Facts.associateWith { factId ->
            FactMastery(factId, 1, "", 5, 0) // Mastered
        }.toMutableMap()

        // By providing a Random generator that always returns a high value,
        // we force the provider to choose a review question.
        val controlledRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float = 0.9f // This will trigger the 'else' branch for review
        }

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = controlledRandom)
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe "ADD_SUM_5" // Should be from the mastered level
    }

    @Test
    fun `initial working set is created for new level`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = emptyMap<String, FactMastery>().toMutableMap()

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(10) {
            workingSet.add(provider.getNextExercise().getFactId())
        }

        workingSet.size shouldBe 5
    }

    @Test
    fun `working set is limited to weakest facts`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf<String, FactMastery>()
        // Create 7 unmastered facts
        for (i in 0..6) {
            userMastery["ADDITION_1_$i"] = FactMastery("ADDITION_1_$i", 1, "", i % 5, 0)
        }

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(20) { // Repeat enough times to likely get all facts from the working set
            workingSet.add(provider.getNextExercise().getFactId())
        }

        workingSet.size shouldBe 5
        // Check that the working set contains only weak facts
        for (factId in workingSet) {
            val strength = userMastery[factId]?.strength ?: 0
            (strength < 5) shouldBe true
        }
    }

    @Test
    fun `provider creates division exercise`() {
        val curriculum = Curriculum.getLevelsFor(Operation.DIVISION)
        val userMastery = emptyMap<String, FactMastery>().toMutableMap()

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe "DIV_BY_2"
        exercise.equation.shouldBeInstanceOf<Division>()
    }

    @Test
    fun `mastered fact is replaced by new fact`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf<String, FactMastery>()
        // Create 4 unmastered facts
        for (i in 0..3) {
            userMastery["ADDITION_1_$i"] =
                FactMastery("ADDITION_1_$i", 1, "", lastTestedTimestamp = 0)
        }
        // Create 1 mastered fact
        userMastery["ADDITION_1_4"] = FactMastery("ADDITION_1_4", 1, "", 5, 0)

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val workingSet = mutableSetOf<String>()
        repeat(20) {
            workingSet.add(provider.getNextExercise().getFactId())
        }

        workingSet.size shouldBe 5
        // Check that the mastered fact is not in the working set
        workingSet.contains("ADDITION_1_4").shouldBeFalse()
    }

    @Test
    fun `unlocked level is the next one after mastered`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = mutableMapOf<String, FactMastery>()

        // Master the first level
        val level1Facts = curriculum[0].getAllPossibleFactIds()
        level1Facts.forEach { factId ->
            userMastery[factId] = FactMastery(factId, 1, "", 5, 0)
        }

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe curriculum[1].id
    }

    @Test
    fun `level with unmastered prerequisites is locked`() {
        val curriculum = Curriculum.getAllLevels()
        val userMastery = emptyMap<String, FactMastery>().toMutableMap()

        val provider = SmartPracticeStrategy(curriculum, userMastery, 1L, random = Random(123))
        val exercise = provider.getNextExercise()

        // The provider should not be able to select a level with prerequisites
        val exerciseLevel = Curriculum.getLevelForExercise(exercise)
        exerciseLevel?.id shouldBe curriculum[0].id
    }
}
