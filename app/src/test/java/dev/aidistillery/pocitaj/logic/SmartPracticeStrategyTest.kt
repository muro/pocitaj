package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method

class SmartPracticeStrategyTest {

    private val level1 = object : Level {
        override val id = "LEVEL_1"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.DRILL
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = listOf("ADD_1_1", "ADD_1_2")
    }

    @Test
    fun `isLevelMastered returns true when all facts are at mastery strength`() {
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, 5, 0)
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery)
        assertTrue(strategy.isLevelMastered(level1))
    }

    @Test
    fun `isLevelMastered returns false when a fact is not at mastery strength`() {
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, 4, 0) // Not mastered
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery)
        assertFalse(strategy.isLevelMastered(level1))
    }

    @Test
    fun `isLevelMastered returns false when a fact is missing from mastery map`() {
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, 5, 0)
            // ADD_1_2 is missing
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery)
        assertFalse(strategy.isLevelMastered(level1))
    }

    @Test
    fun `isLevelUnlocked returns true for a level with no prerequisites`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf())
        assertTrue(strategy.isLevelUnlocked(level1))
    }

    @Test
    fun `isLevelUnlocked returns true when prerequisites are mastered`() {
        val level2 = object : Level by level1 {
            override val id = "LEVEL_2"
            override val prerequisites = setOf("LEVEL_1")
        }
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, 5, 0)
        )
        val strategy = SmartPracticeStrategy(listOf(level1, level2), userMastery)
        assertTrue(strategy.isLevelUnlocked(level2))
    }

    @Test
    fun `isLevelUnlocked returns false when prerequisites are not mastered`() {
        val level2 = object : Level by level1 {
            override val id = "LEVEL_2"
            override val prerequisites = setOf("LEVEL_1")
        }
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, 4, 0) // Not mastered
        )
        val strategy = SmartPracticeStrategy(listOf(level1, level2), userMastery)
        assertFalse(strategy.isLevelUnlocked(level2))
    }
}