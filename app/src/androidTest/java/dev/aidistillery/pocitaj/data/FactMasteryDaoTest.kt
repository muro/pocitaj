package dev.aidistillery.pocitaj.data

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
class FactMasteryDaoTest {
    private lateinit var factMasteryDao: FactMasteryDao
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
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

        val factMastery = FactMastery(
            factId = "ADDITION_5_3",
            userId = userId,
            strength = 3,
            lastTestedTimestamp = System.currentTimeMillis()
        )
        factMasteryDao.upsert(factMastery)
        val retrievedFactMastery = factMasteryDao.getFactMastery(userId, "ADDITION_5_3")
        assertEquals(factMastery.strength, retrievedFactMastery?.strength)

        val updatedFactMastery = factMastery.copy(strength = 4)
        factMasteryDao.upsert(updatedFactMastery)
        val updatedRetrievedFactMastery = factMasteryDao.getFactMastery(userId, "ADDITION_5_3")
        assertEquals(updatedFactMastery.strength, updatedRetrievedFactMastery?.strength)
    }
}
