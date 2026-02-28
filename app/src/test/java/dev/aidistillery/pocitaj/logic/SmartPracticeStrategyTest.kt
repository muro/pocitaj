package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

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
            "ADD_1_1" to FactMastery("ADD_1_1", 1, "", 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, "", 5, 0)
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery, 1L)
        strategy.isLevelMastered(level1).shouldBeTrue()
    }

    @Test
    fun `isLevelMastered returns false when a fact is not at mastery strength`() {
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, "", 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, "", 4, 0) // Not mastered
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery, 1L)
        strategy.isLevelMastered(level1).shouldBeFalse()
    }

    @Test
    fun `isLevelMastered returns false when a fact is missing from mastery map`() {
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, "", 5, 0)
            // ADD_1_2 is missing
        )
        val strategy = SmartPracticeStrategy(listOf(level1), userMastery, 1L)
        strategy.isLevelMastered(level1).shouldBeFalse()
    }

    @Test
    fun `isLevelUnlocked returns true for a level with no prerequisites`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        strategy.isLevelUnlocked(level1).shouldBeTrue()
    }

    @Test
    fun `isLevelUnlocked returns true when prerequisites are mastered`() {
        val level2 = object : Level by level1 {
            override val id = "LEVEL_2"
            override val prerequisites = setOf("LEVEL_1")
        }
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, "", 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, "", 5, 0)
        )
        val strategy = SmartPracticeStrategy(listOf(level1, level2), userMastery, 1L)
        strategy.isLevelUnlocked(level2).shouldBeTrue()
    }

    @Test
    fun `isLevelUnlocked returns false when prerequisites are not mastered`() {
        val level2 = object : Level by level1 {
            override val id = "LEVEL_2"
            override val prerequisites = setOf("LEVEL_1")
        }
        val userMastery = mutableMapOf(
            "ADD_1_1" to FactMastery("ADD_1_1", 1, "", 5, 0),
            "ADD_1_2" to FactMastery("ADD_1_2", 1, "", 4, 0) // Not mastered
        )
        val strategy = SmartPracticeStrategy(listOf(level1, level2), userMastery, 1L)
        strategy.isLevelUnlocked(level2).shouldBeFalse()
    }

    @Test
    fun `calculateMasteryUpdate updates avgDuration using EMA`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        val initialMastery = FactMastery("FACT_1", 1L, "LEVEL_1", 3, 0L, avgDurationMs = 1000L)
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.fromEpochMilliseconds(2000L)

        // New duration 2000. EMA: 0.8 * 1000 + 0.2 * 2000 = 800 + 400 = 1200
        val updated =
            strategy.calculateMasteryUpdate(initialMastery, true, 2000L, SpeedBadge.NONE, clock)
        updated.avgDurationMs shouldBe 1200L
    }

    @Test
    fun `calculateMasteryUpdate uses new duration as initial avg if old was 0`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        val initialMastery = FactMastery("FACT_1", 1L, "LEVEL_1", 3, 0L, avgDurationMs = 0L)
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.fromEpochMilliseconds(2000L)

        val updated =
            strategy.calculateMasteryUpdate(initialMastery, true, 1500L, SpeedBadge.NONE, clock)
        updated.avgDurationMs shouldBe 1500L
    }

    @Test
    fun `calculateMasteryUpdate resets strength to 1 on failure`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        val initialMastery = FactMastery("FACT_1", 1L, "LEVEL_1", 5, 0L)
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.fromEpochMilliseconds(2000L)

        val updated =
            strategy.calculateMasteryUpdate(initialMastery, false, 1000L, SpeedBadge.GOLD, clock)
        updated.strength shouldBe 1
    }

    @Test
    fun `calculateMasteryUpdate performs Fast Track jump on GOLD badge`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.fromEpochMilliseconds(2000L)

        // Case 1: Jump from low strength (e.g. 1) to Consolidating (4)
        val mastery1 = FactMastery("FACT_1", 1L, "LEVEL_1", 1, 0L)
        strategy.calculateMasteryUpdate(
            mastery1,
            true,
            500L,
            SpeedBadge.GOLD,
            clock
        ).strength shouldBe 4

        // Case 2: Jump from Consolidating (4) to Target (5)
        val mastery4 = FactMastery("FACT_1", 1L, "LEVEL_1", 4, 0L)
        strategy.calculateMasteryUpdate(
            mastery4,
            true,
            500L,
            SpeedBadge.GOLD,
            clock
        ).strength shouldBe 5
    }

    @Test
    fun `calculateMasteryUpdate increments strength normally for silver or lower`() {
        val strategy = SmartPracticeStrategy(listOf(level1), mutableMapOf(), 1L)
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.fromEpochMilliseconds(2000L)

        // Silver badge at level 3 -> promotes to 4
        val mastery3 = FactMastery("FACT_1", 1L, "LEVEL_1", 3, 0L)
        strategy.calculateMasteryUpdate(
            mastery3,
            true,
            500L,
            SpeedBadge.SILVER,
            clock
        ).strength shouldBe 4

        // Silver badge at level 2 -> stays at 2 (Silver requires level 3 to promote)
        // Wait, checking code: currentStrength == 2 -> if (speedBadge >= SpeedBadge.BRONZE) 3 else 2
        // So Silver SHOULD promote 2 to 3.
        val mastery2 = FactMastery("FACT_1", 1L, "LEVEL_1", 2, 0L)
        strategy.calculateMasteryUpdate(
            mastery2,
            true,
            500L,
            SpeedBadge.SILVER,
            clock
        ).strength shouldBe 3
    }

    @Test
    fun `creating strategy with an empty level throws exception`() {
        val emptyLevel = object : Level by level1 {
            override fun getAllPossibleFactIds() = emptyList<String>()
        }
        shouldThrow<IllegalArgumentException> {
            SmartPracticeStrategy(listOf(emptyLevel), mutableMapOf(), 1L)
        }
    }
}