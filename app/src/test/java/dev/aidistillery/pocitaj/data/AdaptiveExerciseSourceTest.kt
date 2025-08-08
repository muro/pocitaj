package dev.aidistillery.pocitaj.data

import dev.aidistillery.pocitaj.logic.Curriculum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AdaptiveExerciseSourceTest {

    private lateinit var factMasteryDao: FakeFactMasteryDao
    private lateinit var exerciseAttemptDao: FakeExerciseAttemptDao
    private lateinit var userDao: FakeUserDao
    private lateinit var exerciseSource: AdaptiveExerciseSource

    // --- Test Doubles ---
    class FakeFactMasteryDao : FactMasteryDao {
        private val facts = mutableMapOf<String, FactMastery>()
        private val flow = MutableStateFlow<List<FactMastery>>(emptyList())
        override fun getAllFactsForUser(userId: Long) = flow.asStateFlow()
        override suspend fun getFactMastery(userId: Long, factId: String) = facts[factId]
        override suspend fun upsert(factMastery: FactMastery) {
            facts[factMastery.factId] = factMastery
            flow.value = facts.values.toList()
        }
    }

    class FakeExerciseAttemptDao : ExerciseAttemptDao {
        val attempts = mutableListOf<ExerciseAttempt>()
        override suspend fun insert(attempt: ExerciseAttempt) {
            attempts.add(attempt)
        }

        override fun getAttemptsForUser(userId: Long) =
            MutableStateFlow(attempts.toList()).asStateFlow()
    }

    class FakeUserDao : UserDao {
        private val users = mutableMapOf<Long, User>()
        private var nextId = 1L
        override suspend fun insert(user: User): Long {
            val idToInsert = user.id.takeIf { it != 0L } ?: nextId++
            users[idToInsert] = user.copy(id = idToInsert)
            return idToInsert
        }

        override suspend fun getUser(id: Long): User? = users[id]
        override suspend fun getUserByName(name: String): User? =
            users.values.find { it.name == name }

        override fun getAllUsers() = MutableStateFlow(users.values.toList()).asStateFlow()
    }

    @Before
    fun setup() {
        factMasteryDao = FakeFactMasteryDao()
        exerciseAttemptDao = FakeExerciseAttemptDao()
        userDao = FakeUserDao()

        exerciseSource = AdaptiveExerciseSource(
            factMasteryDao,
            exerciseAttemptDao,
            userDao,
            1L
        )
    }

    @Test
    fun `initialize with levelId filters curriculum to a single level`() = runBlocking {
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
        assertEquals("ADD_SUM_5", level?.id)
    }

    @Test
    fun `initialize without levelId filters curriculum by operation`() = runBlocking {
        // ARRANGE
        val config = ExerciseConfig(
            operation = Operation.SUBTRACTION,
            difficulty = 10,
            count = 5,
            levelId = null // No specific level
        )

        // ACT
        exerciseSource.initialize(config)

        // Generate a few exercises to ensure they are all from the correct operation
        repeat(5) {
            val exercise = exerciseSource.getNextExercise()!!
            val level = Curriculum.getLevelForExercise(exercise)
            // ASSERT
            assertEquals(Operation.SUBTRACTION, level?.operation)
        }
    }
}
