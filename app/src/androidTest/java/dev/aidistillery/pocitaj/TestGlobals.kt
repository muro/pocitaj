package dev.aidistillery.pocitaj

import android.content.Context
import androidx.room.Room
import dev.aidistillery.pocitaj.data.AppDatabase
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.FakeActiveUserManager
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserDao

class TestGlobals(private val context: Context) : Globals {
    private val database: AppDatabase by lazy {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }
    override val userDao: UserDao by lazy { database.userDao() }
    override val factMasteryDao: FactMasteryDao by lazy { database.factMasteryDao() }
    override val exerciseAttemptDao: ExerciseAttemptDao by lazy { database.exerciseAttemptDao() }
    override val activeUserManager = FakeActiveUserManager()
    override val exerciseSource: ExerciseSource by lazy {
        ExerciseBook(exerciseAttemptDao, activeUserManager)
    }
    override val inkModelManager: InkModelManager by lazy {
        FakeInkModelManager
    }
    override val activeUser: User
        get() = activeUserManager.activeUser

    fun reset() {
        activeUserManager.reset()
        database.clearAllTables()
    }
}
