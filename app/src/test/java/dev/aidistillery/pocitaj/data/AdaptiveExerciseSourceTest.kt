package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.logic.Curriculum
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AdaptiveExerciseSourceTest {

    private lateinit var factMasteryDao: FakeFactMasteryDao
    private lateinit var exerciseAttemptDao: FakeExerciseAttemptDao
    private lateinit var userDao: FakeUserDao
    private lateinit var exerciseSource: AdaptiveExerciseSource
    private lateinit var activeUserManager: FakeActiveUserManager

    @Before
    fun setup() {
        factMasteryDao = FakeFactMasteryDao()
        exerciseAttemptDao = FakeExerciseAttemptDao()
        userDao = FakeUserDao()
        activeUserManager = FakeActiveUserManager(
            User(
                7,
                "John Doe"
            )
        )
        runBlocking {
            activeUserManager.init()
        }

        exerciseSource = AdaptiveExerciseSource(
            factMasteryDao,
            exerciseAttemptDao,
            activeUserManager
        )
    }

    @Test
    fun `initialize with levelId filters curriculum to a single level`() {
        runBlocking {
            // ARRANGE
            val config = ExerciseConfig(
                operation = Operation.ADDITION,
                difficulty = 10,
                count = 5,
                levelId = "ADD_SUM_5"
            )

            // ACT
            exerciseSource.initialize(config)
            val exercise = exerciseSource.getNextExercise()!!
            val level = Curriculum.getLevelForExercise(exercise)

            // ASSERT
            level?.id shouldBe "ADD_SUM_5"
        }
    }

    @Test
    fun `initialize without levelId filters curriculum by operation`() {
        runBlocking {
            // ARRANGE
            val config = ExerciseConfig(
                operation = Operation.SUBTRACTION,
                difficulty = 10,
                count = 5
            )

            // ACT
            exerciseSource.initialize(config)

            // Generate a few exercises to ensure they are all from the correct operation
            repeat(5) {
                val exercise = exerciseSource.getNextExercise()!!
                val level = Curriculum.getLevelForExercise(exercise)
                // ASSERT
                level?.operation shouldBe Operation.SUBTRACTION
            }
        }
    }

    @Test
    fun `recordAttempt saves mastery for the active level and globally`() {
        runBlocking {
            // ARRANGE
            val levelId = "ADD_SUM_5"
            val config = ExerciseConfig(
                operation = Operation.ADDITION,
                difficulty = 10,
                count = 5,
                levelId = levelId
            )
            exerciseSource.initialize(config)
            val exercise = exerciseSource.getNextExercise()!! // e.g., 1+1

            // ACT
            exerciseSource.recordAttempt(exercise, exercise.equation.getExpectedResult(), 1000L)

            // ASSERT
            val factId = exercise.equation.question()

            // 1. Verify it was saved for the correct level
            val savedMastery = factMasteryDao.getFactMastery(7, factId, levelId)
            savedMastery.shouldNotBeNull()
            savedMastery.level shouldBe levelId

            // 2. Verify it was saved globally (level = "")
            val savedGlobalMastery = factMasteryDao.getFactMastery(7, factId, "")
            savedGlobalMastery.shouldNotBeNull()
            savedGlobalMastery.level shouldBe ""

            // 3. Verify it was NOT saved for other levels containing the same fact (e.g., Doubles)
            val doublesId = Curriculum.Doubles.id
            if (factId == "2 + 2 = ?") {
                val savedMasteryDoubles = factMasteryDao.getFactMastery(7, factId, doublesId)
                savedMasteryDoubles.shouldBeNull()
            }
        }
    }
}


