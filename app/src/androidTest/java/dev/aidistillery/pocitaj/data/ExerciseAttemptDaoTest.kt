package dev.aidistillery.pocitaj.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
    fun insertAndGetAttemptForDate() = runTest {
        val now = System.currentTimeMillis()
        val dateString = java.time.Instant.ofEpochMilli(now)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().toString()

        val attempt = ExerciseAttempt(
            id = 1,
            userId = 1,
            timestamp = now,
            problemText = "2+2",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 4,
            submittedAnswer = 4,
            wasCorrect = true,
            durationMs = 1000
        )
        exerciseAttemptDao.insert(attempt)

        val attempts = exerciseAttemptDao.getAttemptsForDate(1, dateString).first()
        attempts.size shouldBe 1
        attempts[0].id shouldBe attempt.id
        attempts[0].problemText shouldBe attempt.problemText
    }

    @Test
    fun getDailyActivityCounts() = runTest {
        val now = System.currentTimeMillis()
        val todayString = java.time.Instant.ofEpochMilli(now)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().toString()

        // Yesterday (approximate, for testing grouping)
        val yesterday = now - 24 * 60 * 60 * 1000
        val yesterdayString = java.time.Instant.ofEpochMilli(yesterday)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate().toString()

        exerciseAttemptDao.insert(createAttempt(userId = 1, timestamp = now))
        exerciseAttemptDao.insert(createAttempt(userId = 1, timestamp = now + 1000))
        exerciseAttemptDao.insert(createAttempt(userId = 1, timestamp = yesterday))

        val counts = exerciseAttemptDao.getDailyActivityCounts(1).first()
        counts.size shouldBe 2
        counts.find { it.dateString == todayString }?.count shouldBe 2
        counts.find { it.dateString == yesterdayString }?.count shouldBe 1
    }

    private fun createAttempt(userId: Long, timestamp: Long): ExerciseAttempt {
        return ExerciseAttempt(
            userId = userId,
            timestamp = timestamp,
            problemText = "1+1",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 2,
            submittedAnswer = 2,
            wasCorrect = true,
            durationMs = 1000
        )
    }
}
