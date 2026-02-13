package dev.aidistillery.pocitaj.logic

import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        reviewStrength: Int = 5,
        targetStrength: Int = 6,
        userId: Long = 1L
    ): ReviewStrategy {
        return ReviewStrategy(
            level,
            masteryMap,
            reviewStrength = reviewStrength,
            targetStrength = targetStrength,
            activeUserId = userId
        )
    }

    // --- getNextExercise Tests ---

    @Test
    fun `getNextExercise returns a fact when mastery is empty`() {
        val userMastery = mutableMapOf<String, FactMastery>()
        val strategy = setupStrategy(userMastery)
        val exercise = strategy.getNextExercise()
        assertNotNull("Should return a non-null exercise even with no history", exercise)
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

        assertEquals(
            "The overdue fact should be selected in all 20 attempts",
            20,
            overdueSelections
        )
    }

    // --- recordAttempt Tests ---

    @Test
    fun `incorrect answer resets strength to 1`() {
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 7, 100L))
        val strategy = setupStrategy(userMastery)
        val exercise = exerciseFromFactId("1 + 1 = ?")

        strategy.recordAttempt(exercise, wasCorrect = false)

        assertEquals(
            "Strength should be reset to 1 on failure",
            1,
            userMastery["1 + 1 = ?"]!!.strength
        )
    }

    @Test
    fun `correct answer with Gold speed promotes to targetStrength`() {
        val userMastery =
            mutableMapOf("1 + 2 = ?" to FactMastery("1 + 2 = ?", 1, "", 6, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("1 + 2 = ?")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should be promoted to targetStrength",
            7,
            userMastery["1 + 2 = ?"]!!.strength
        )
    }

    @Test
    fun `correct answer with Gold speed does not promote beyond targetStrength`() {
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 7, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.GOLD

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should not exceed targetStrength",
            7,
            userMastery["1 + 1 = ?"]!!.strength
        )
    }

    @Test
    fun `correct answer with Silver speed demotes strength by 1`() {
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 7, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.SILVER

        strategy.recordAttempt(exercise, true)

        assertEquals("Strength should be demoted by 1", 6, userMastery["1 + 1 = ?"]!!.strength)
    }

    @Test
    fun `correct answer with Bronze speed does not demote below reviewStrength`() {
        val userMastery = mutableMapOf("1 + 1 = ?" to FactMastery("1 + 1 = ?", 1, "", 6, 100L))
        val strategy = setupStrategy(userMastery, reviewStrength = 6, targetStrength = 7)
        val exercise = exerciseFromFactId("1 + 1 = ?")
        exercise.speedBadge = SpeedBadge.BRONZE

        strategy.recordAttempt(exercise, true)

        assertEquals(
            "Strength should not go below reviewStrength",
            6,
            userMastery["1 + 1 = ?"]!!.strength
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

// Helper function copied from DrillStrategyTest
private fun exerciseFromFactId(factId: String): Exercise {
    // 1 + 1 = ?
    val regex = Regex("""(\d+) ([+\-*/]) (\d+) = \?""")
    val match = regex.matchEntire(factId) ?: throw IllegalArgumentException("Invalid ID: $factId")
    val (op1Str, opStr, op2Str) = match.destructured
    val first = op1Str.toInt()
    val second = op2Str.toInt()

    return when (opStr) {
        "+" -> Exercise(Addition(first, second))
        "-" -> Exercise(Subtraction(first, second))
        "*" -> Exercise(Multiplication(first, second))
        "/" -> Exercise(Division(first, second))
        else -> throw IllegalArgumentException("Unknown op: $opStr")
    }
}