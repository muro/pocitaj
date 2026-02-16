package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test

class ReviewStrategyTest {

    // A standard level with 10 facts for all tests
    private val testLevel = object : Level {
        override val id = "TEST_LEVEL"
        override val operation = Operation.ADDITION
        override val prerequisites = emptySet<String>()
        override val strategy = ExerciseStrategy.REVIEW // Important: Set strategy to REVIEW
        override fun generateExercise() = Exercise(Addition(1, 1))
        override fun getAllPossibleFactIds() = (1..10).map { "1 + $it = ?" }
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
        userId: Long = 1L
    ): ReviewStrategy {
        return ReviewStrategy(
            level,
            masteryMap,
            activeUserId = userId
        )
    }

    // --- getNextExercise Tests ---

    @Test
    fun `getNextExercise returns a fact when mastery is empty`() {
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = setupStrategy(userMastery)
        val exercise = strategy.getNextExercise()
        withClue("Should return a non-null exercise even with no history") {
            exercise.shouldNotBeNull()
        }
    }

    @Test
    fun `prioritizes overdue facts over recently tested facts`() {
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - 7 * 24 * 60 * 60 * 1000
        val fiveMinutesAgo = now - 5 * 60 * 1000
        val overdueFactId = "1 + 1 = ?"
        val recentFactId = "1 + 2 = ?"
        val userMastery = mutableMapOf(
            createMastery(overdueFactId, 1, oneWeekAgo),
            createMastery(recentFactId, 4, fiveMinutesAgo)
        )
        val strategy = setupStrategy(userMastery)

        val selections = (1..20).mapNotNull { strategy.getNextExercise()?.getFactId() }
        val overdueSelections = selections.count { it == overdueFactId }

        withClue("The overdue fact should be selected in all 20 attempts") {
            overdueSelections shouldBe 20
        }
    }

    // --- recordAttempt Tests ---

    @Test
    fun `incorrect answer resets strength to 1`() {
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 4, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = testLevel.createExercise("1 + 1 = ?")

        strategy.recordAttempt(exercise, wasCorrect = false)

        withClue("Strength should be reset to 1 on failure") {
            userMastery["1 + 1 = ?"]!!.strength shouldBe 1
        }
    }

    @Test
    fun `correct answer with Gold speed promotes to targetStrength (5)`() {
        // Start at 4 (Consolidating). Gold should push to 5 (Mastered).
        val userMastery =
            mutableMapOf("1 + 2 = ?" to FactMastery("1 + 2 = ?", 1, "", 4, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = testLevel.createExercise("1 + 2 = ?")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        withClue("Strength should be promoted to 5") {
            userMastery["1 + 2 = ?"]!!.strength shouldBe 5
        }
    }

    @Test
    fun `correct answer with Gold speed does not promote beyond targetStrength (5)`() {
        // Start at 5. Gold should keep it at 5.
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 5, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = testLevel.createExercise("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        withClue("Strength should not exceed 5") {
            userMastery["1 + 1 = ?"]!!.strength shouldBe 5
        }
    }

    @Test
    fun `correct answer with Silver speed at max strength remains at max`() {
        // Start at 5. Silver should maintain 5 (no demotion).
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 5, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = testLevel.createExercise("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.SILVER

        strategy.recordAttempt(exercise, true)

        withClue("Strength should remain at 5") {
            userMastery["1 + 1 = ?"]!!.strength shouldBe 5
        }
    }

    @Test
    fun `correct answer with Bronze speed at consolidating strength remains at consolidating`() {
        // Start at 4. Bronze should maintain 4.
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 4, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = testLevel.createExercise("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.BRONZE

        strategy.recordAttempt(exercise, true)

        withClue("Strength should remain at 4") {
            userMastery["1 + 1 = ?"]!!.strength shouldBe 4
        }
    }

    @Test
    fun `creating strategy with an empty level throws exception`() {
        val emptyLevel = object : Level by testLevel {
            override fun getAllPossibleFactIds() = emptyList<String>()
        }
        shouldThrow<IllegalArgumentException> {
            setupStrategy(mutableMapOf(), level = emptyLevel)
        }
    }
}