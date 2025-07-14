package com.codinglikeapirate.pocitaj.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ExerciseAttemptDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var exerciseAttemptDao: ExerciseAttemptDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        exerciseAttemptDao = database.exerciseAttemptDao()
        userDao = database.userDao()
        runBlocking {
            userDao.insert(User(id = 1, name = "Default User"))
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetAttempt() = runTest {
        val attempt = ExerciseAttempt(
            id = 1,
            userId = 1,
            timestamp = System.currentTimeMillis(),
            problemText = "2+2",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 4,
            submittedAnswer = 4,
            wasCorrect = true,
            durationMs = 1000
        )
        exerciseAttemptDao.insert(attempt)

        val attempts = exerciseAttemptDao.getAttemptsForUser(1).first()
        assertEquals(1, attempts.size)
        assertEquals(attempt, attempts[0])
    }
}
