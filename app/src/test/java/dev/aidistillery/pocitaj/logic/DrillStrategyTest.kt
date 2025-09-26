package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrillStrategyTest {

    // A standard level with 10 facts for most tests
    private val testLevel = object : Level {
        override val id = "TEST_LEVEL"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.DRILL
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = (1..10).map { "ADDITION_1_${it}" }
    }

    // --- Helper Functions ---

    private fun createMastery(
        factId: String,
        strength: Int,
        lastTested: Long
    ): Pair<String, FactMastery> {
        return factId to FactMastery(factId, 1, "", strength, lastTested)
    }

    private fun setupStrategy(
        masteryMap: MutableMap<String, FactMastery>,
        level: Level = testLevel,
        setSize: Int = 4,
        userId: Long = 1L
    ): DrillStrategy {
        return DrillStrategy(level, masteryMap, setSize, activeUserId = userId)
    }

    // --- New Initialization Tests ---

    @Test
    fun `initial working set prioritizes L1 facts`() {
        // ARRANGE: 5 L1 facts, more than the working set size of 4.
        val userMastery = mutableMapOf(
            createMastery("ADDITION_1_1", 0, 100L),
            createMastery("ADDITION_1_2", 1, 200L),
            createMastery("ADDITION_1_3", 2, 50L),
            createMastery("ADDITION_1_4", 0, 400L),
            createMastery("ADDITION_1_5", 1, 300L)
        )
        val strategy = setupStrategy(userMastery)

        // ACT
        val workingSet = strategy.workingSet

        // ASSERT
        assertEquals("Working set should be the target size", 4, workingSet.size)
        assertTrue(
            "All facts in the working set must be L1 facts",
            workingSet.all { (userMastery[it]?.strength ?: 0) < 3 }
        )
    }

    @Test
    fun `initial working set fills with L2 facts when L1 isn't enough`() {
        // ARRANGE: 2 L1 facts and 3 L2 facts.
        val userMastery = mutableMapOf(
            createMastery("ADDITION_1_1", 0, 100L), // L1
            createMastery("ADDITION_1_2", 1, 200L), // L1
            createMastery("ADDITION_1_3", 3, 50L),  // L2 (oldest)
            createMastery("ADDITION_1_4", 4, 400L), // L2
            createMastery("ADDITION_1_5", 3, 150L)  // L2 (second oldest)
        )
        val strategy = setupStrategy(userMastery)

        // ACT
        val workingSet = strategy.workingSet

        // ASSERT
        assertEquals("Working set should be the target size", 4, workingSet.size)
        assertTrue(
            "Must contain all L1 facts",
            workingSet.containsAll(listOf("ADDITION_1_1", "ADDITION_1_2"))
        )
        assertTrue(
            "Must contain the two oldest L2 facts",
            workingSet.containsAll(listOf("ADDITION_1_3", "ADDITION_1_5"))
        )
        assertFalse("Should not contain the newest L2 fact", workingSet.contains("ADDITION_1_4"))
    }

    @Test
    fun `initial working set fills with unseen facts when L1 and L2 arent enough`() {
        // ARRANGE: 1 L1 fact and 1 L2 fact.
        val userMastery = mutableMapOf(
            createMastery("ADDITION_1_1", 1, 100L), // L1
            createMastery("ADDITION_1_2", 3, 200L)  // L2
        )
        val strategy = setupStrategy(userMastery)

        // ACT
        val workingSet = strategy.workingSet
        val unseenFactsInSet = workingSet.filter { !userMastery.containsKey(it) }

        // ASSERT
        assertEquals("Working set should be the target size", 4, workingSet.size)
        assertTrue("Must contain the L1 fact", workingSet.contains("ADDITION_1_1"))
        assertTrue("Must contain the L2 fact", workingSet.contains("ADDITION_1_2"))
        assertEquals("Should be filled with 2 unseen facts", 2, unseenFactsInSet.size)
    }

    @Test
    fun `initial working set handles fewer facts than set size in entire level`() {
        // ARRANGE: A level that only has 3 facts in total.
        val smallLevel = object : Level by testLevel {
            override fun getAllPossibleFactIds() = listOf("A", "B", "C")
        }
        val strategy = setupStrategy(mutableMapOf(), level = smallLevel)

        // ACT
        val workingSet = strategy.workingSet

        // ASSERT
        assertEquals(
            "Working set size should be the total number of facts in the level",
            3,
            workingSet.size
        )
        assertTrue(
            "Working set should contain all facts from the level",
            workingSet.containsAll(listOf("A", "B", "C"))
        )
    }

    @Test
    fun `initial working set handles fewer facts than set size available to drill`() {
        // ARRANGE: A level with 10 facts, but 8 are already mastered.
        val userMastery = (3..10).map {
            createMastery("ADDITION_1_$it", 5, 1000L) // Mastered
        }.toMap().toMutableMap()
        userMastery.putAll(
            mapOf(
                createMastery("ADDITION_1_1", 1, 100L), // L1
                createMastery("ADDITION_1_2", 3, 200L)  // L2
            )
        )
        val strategy = setupStrategy(userMastery)

        // ACT
        val workingSet = strategy.workingSet
        val masteredFactsInSet = workingSet.filter { (userMastery[it]?.strength ?: 0) >= 5 }

        // ASSERT
        assertEquals("Working set should be full (2 unmastered + 2 mastered)", 4, workingSet.size)
        assertTrue(
            "Working set must contain the L1 and L2 facts",
            workingSet.containsAll(listOf("ADDITION_1_1", "ADDITION_1_2"))
        )
        assertEquals(
            "Working set should be filled with 2 mastered L3 facts",
            2,
            masteredFactsInSet.size
        )
    }


    // --- Existing Tests for `recordAttempt` ---


    @Test
    fun `incorrect answer to L2 fact demotes it to strength 1`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, "", 4, 100L))
        val strategy = setupStrategy(userMastery, setSize = 1)
        assertEquals(listOf("ADDITION_1_1"), strategy.workingSet)
        val exercise = exerciseFromFactId("ADDITION_1_1")

        strategy.recordAttempt(exercise, false)

        assertEquals(1, userMastery["ADDITION_1_1"]!!.strength)
    }

    @Test
    fun `recordAttempt for new fact assigns correct userId`() {
        // ARRANGE
        val userMastery = mutableMapOf<String, FactMastery>()
        val testUserId = 7L
        val strategy = setupStrategy(userMastery, userId = testUserId)
        val exercise = exerciseFromFactId("ADDITION_1_1")

        // ACT
        val (newMastery, _) = strategy.recordAttempt(exercise, wasCorrect = true)

        // ASSERT
        assertNotNull("Should return a new mastery object", newMastery)
        assertEquals(
            "The new mastery should be assigned to the correct user",
            testUserId,
            newMastery!!.userId
        )
    }

    @Test
    fun `strength 2 to 3 does not happen if speed is too slow`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, "", 2, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.NONE // Too slow

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should not increase without the required speed",
            2,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test
    fun `strength 2 to 3 requires bronze badge`() {
        val userMastery = mutableMapOf("ADDITION_1_1" to FactMastery("ADDITION_1_1", 1, "", 2, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = exerciseFromFactId("ADDITION_1_1")
        exercise.speedBadge = SpeedBadge.BRONZE

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should advance to 3 with a Bronze badge",
            3,
            userMastery["ADDITION_1_1"]!!.strength
        )
    }

    @Test
    fun `get next exercise serves random L3 facts when all facts are mastered`() {
        // ARRANGE: A user who has mastered every fact in the level.
        val userMastery = testLevel.getAllPossibleFactIds().associateWith {
            FactMastery(it, 1, "", 5, System.currentTimeMillis())
        }.toMutableMap()
        val strategy = setupStrategy(userMastery)

        val selectedFacts = mutableListOf<String>()

        // ACT & ASSERT: Run a long session to observe the selection pattern.
        for (i in 1..50) {
            val exercise = strategy.getNextExercise()
            assertNotNull(
                "Should always provide a review exercise, even when all facts are mastered (iteration $i)",
                exercise
            )
            selectedFacts.add(exercise!!.getFactId())
            strategy.recordAttempt(exercise, true)
        }

        // FINAL ASSERT: Check that the selection was varied and sampled from the whole level.
        val uniqueSelectedFacts = selectedFacts.toSet()
        assertTrue(
            "The selection should be varied and not stuck on the initial working set. Unique count: ${uniqueSelectedFacts.size}",
            uniqueSelectedFacts.size > 4
        )
    }

    @Test
    fun `mastered fact is replaced by next weakest fact`() {
        // ARRANGE: A working set with 2 L1 facts and 2 L2 facts.
        // One of the L2 facts is old and ready to be mastered.
        // The "next weakest" fact is an unseen one.
        val userMastery = mutableMapOf(
            createMastery("ADDITION_1_2", 0, 100L), // L1
            createMastery("ADDITION_1_3", 1, 200L), // L1
            createMastery("ADDITION_1_1", 4, 50L),  // L2 (oldest, will be mastered)
            createMastery("ADDITION_1_4", 3, 400L)  // L2
        )
        val strategy = setupStrategy(userMastery)
        val initialWorkingSet = strategy.workingSet

        // Sanity-check the initial state
        assertTrue(
            "Fact to be mastered must be in the initial set",
            initialWorkingSet.contains("ADDITION_1_1")
        )
        assertEquals(4, initialWorkingSet.size)

        // ACT: Master the L2 fact
        strategy.recordAttempt(exerciseFromFactId("ADDITION_1_1"), true)

        // ASSERT
        val finalWorkingSet = strategy.workingSet
        val unseenFactsInSet = finalWorkingSet.filter { !userMastery.containsKey(it) }

        assertEquals("Working set should maintain its size", 4, finalWorkingSet.size)
        assertFalse("Mastered fact should be removed", finalWorkingSet.contains("ADDITION_1_1"))
        assertEquals(
            "A new, unseen fact should have been added to fill the space",
            1,
            unseenFactsInSet.size
        )
    }

    @Test
    fun `working set remains full during a long, successful session`() {
        // ARRANGE: Start with a fresh level and no mastery.
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = setupStrategy(userMastery)

        // ACT & ASSERT: Simulate a long session of correct answers.
        for (i in 1..20) {
            // Get an exercise; fail if the provider dries up prematurely.
            val exercise = strategy.getNextExercise()
            assertNotNull(
                "Strategy should always provide an exercise when unmastered facts remain (iteration $i)",
                exercise
            )

            // Answer correctly.
            strategy.recordAttempt(exercise!!, true)
        }

        // FINAL ASSERT: The working set should still be full.
        assertEquals(
            "Working set should be replenished and remain full",
            4, strategy.workingSet.size
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `creating strategy with an empty level throws exception`() {
        val emptyLevel = object : Level by testLevel {
            override fun getAllPossibleFactIds() = emptyList<String>()
        }
        setupStrategy(mutableMapOf(), level = emptyLevel)
    }
}




