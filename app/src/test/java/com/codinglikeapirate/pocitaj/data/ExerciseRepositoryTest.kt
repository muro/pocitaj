package com.codinglikeapirate.pocitaj.data

import com.codinglikeapirate.pocitaj.logic.Addition
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Exercise
import com.codinglikeapirate.pocitaj.logic.Multiplication
import com.codinglikeapirate.pocitaj.logic.Subtraction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ExerciseRepositoryTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var factMasteryDao: FactMasteryDao
    private lateinit var exerciseAttemptDao: ExerciseAttemptDao
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        factMasteryDao = mockk(relaxed = true)
        exerciseAttemptDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        repository = ExerciseRepository(factMasteryDao, exerciseAttemptDao, userDao)
    }

    @Test
    fun `getNextExercise returns addition when specified`() = runTest {
        coEvery { factMasteryDao.getAllFactsForUser(any()) } returns emptyList()
        repository.startSession(ExerciseConfig("addition", 10, 10))
        val exercise = repository.getNextExercise(1L)
        assertTrue(exercise.equation is Addition)
    }

    @Test
    fun `getNextExercise returns subtraction when specified`() = runTest {
        coEvery { factMasteryDao.getAllFactsForUser(any()) } returns emptyList()
        repository.startSession(ExerciseConfig("subtraction", 10, 10))
        val exercise = repository.getNextExercise(1L)
        assertTrue(exercise.equation is Subtraction)
    }

    @Test
    fun `recordAttempt inserts attempt and updates mastery`() = runTest {
        val exercise = Exercise(Multiplication(2, 3))
        repository.recordAttempt(1L, exercise, 6, 1000)
        coVerify { exerciseAttemptDao.insert(any()) }
        coVerify { factMasteryDao.upsert(any()) }
    }

    @Test
    fun `startSession filters levels for the provider`() = runTest {
        coEvery { factMasteryDao.getAllFactsForUser(any()) } returns emptyList()
        val config = ExerciseConfig("multiplication", 10, 10)
        repository.startSession(config)
        val exercise = repository.getNextExercise(1L)
        assertEquals(Operation.MULTIPLICATION, exercise.getFactId().let { Operation.valueOf(it.split("_")[0]) })
    }
}
