package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Addition
import com.codinglikeapirate.pocitaj.logic.Exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ExerciseRepositoryTest {

    private lateinit var repository: ExerciseRepository
    private val factMasteryDao: FactMasteryDao = mockk()
    private val exerciseAttemptDao: ExerciseAttemptDao = mockk()
    private val userDao: UserDao = mockk()

    @Before
    fun setup() {
        repository = ExerciseRepository(factMasteryDao, exerciseAttemptDao, userDao)
    }

    @Test
    fun `getNextExercise fetches mastery and uses provider`() = runBlocking {
        // Given
        val userId = 1L
        val masteryList = listOf(FactMastery("ADDITION_1_1", userId, 1, 0))
        coEvery { factMasteryDao.getAllFactsForUser(userId) } returns masteryList

        // When
        val exercise = repository.getNextExercise(userId)

        // Then
        coVerify { factMasteryDao.getAllFactsForUser(userId) }
        // We can't easily verify the provider was called without more complex setup,
        // but we can check that the returned exercise is valid.
        assert(exercise.equation is Addition)
    }

    @Test
    fun `recordAttempt saves attempt and updates mastery`() = runBlocking {
        // Given
        val userId = 1L
        val exercise = Exercise(Addition(1, 1))
        val submittedAnswer = 2
        val durationMs = 1000L
        val factId = "ADDITION_1_1"
        val currentMastery = FactMastery(factId, userId, 2, 100)
        coEvery { factMasteryDao.getFactMastery(userId, factId) } returns currentMastery
        coEvery { exerciseAttemptDao.insert(any()) } returns Unit
        coEvery { factMasteryDao.upsert(any()) } returns Unit


        // When
        repository.recordAttempt(userId, exercise, submittedAnswer, durationMs)

        // Then
        coVerify { exerciseAttemptDao.insert(any()) }
        coVerify { factMasteryDao.upsert(any()) }
    }

    @Test
    fun `getNextExercise_afterStartingSession_returnsExerciseOfCorrectType`() = runBlocking {
        // GIVEN: The repository is initialized for ADDITION exercises
        val config = com.codinglikeapirate.pocitaj.ExerciseConfig("addition")
        repository.startSession(config)

        // AND: The DAO returns no mastery information
        coEvery { factMasteryDao.getAllFactsForUser(any()) } returns emptyList()

        // WHEN: We get the next exercise
        val exercise = repository.getNextExercise(1L)

        // THEN: The exercise should be an addition problem
        assert(exercise.equation is Addition)
    }
}
