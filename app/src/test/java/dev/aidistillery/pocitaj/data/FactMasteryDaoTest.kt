package dev.aidistillery.pocitaj.data

import androidx.room.Room
import dev.aidistillery.pocitaj.logic.Curriculum
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FactMasteryDaoTest {
    private lateinit var factMasteryDao: FactMasteryDao
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        factMasteryDao = db.factMasteryDao()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun upsertAndGetFactMasteryMastery() = runBlocking {
        val user = User(name = "test_user")
        val userId = userDao.insert(user)
        val level = Curriculum.SubtractingTens.id

        val factMastery = FactMastery(
            factId = "ADDITION_5_3",
            userId = userId,
            level = level,
            strength = 3,
            lastTestedTimestamp = System.currentTimeMillis()
        )
        factMasteryDao.upsert(factMastery)
        val retrievedFactMastery = factMasteryDao.getFactMastery(userId, "ADDITION_5_3", level)
        assertEquals(factMastery.strength, retrievedFactMastery?.strength)

        val updatedFactMastery = factMastery.copy(strength = 4)
        factMasteryDao.upsert(updatedFactMastery)
        val updatedRetrievedFactMastery =
            factMasteryDao.getFactMastery(userId, "ADDITION_5_3", level)
        assertEquals(updatedFactMastery.strength, updatedRetrievedFactMastery?.strength)
    }
}
