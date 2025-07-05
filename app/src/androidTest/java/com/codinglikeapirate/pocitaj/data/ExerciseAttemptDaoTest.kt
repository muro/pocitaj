package com.codinglikeapirate.pocitaj.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ExerciseAttemptDaoTest {
    private lateinit var exerciseAttemptDao: ExerciseAttemptDao
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        exerciseAttemptDao = db.exerciseAttemptDao()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetAttempt() = runBlocking {
        val user = User(name = "test_user")
        val userId = userDao.insert(user)

        val attempt = ExerciseAttempt(
            userId = userId,
            timestamp = System.currentTimeMillis(),
            problemText = "5 + 3",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 8,
            submittedAnswer = 8,
            wasCorrect = true,
            durationMs = 1000
        )
        exerciseAttemptDao.insert(attempt)
        val retrievedAttempts = exerciseAttemptDao.getAttemptsForUser(userId)
        assertEquals(1, retrievedAttempts.size)
        assertEquals(attempt.problemText, retrievedAttempts[0].problemText)
    }
}
