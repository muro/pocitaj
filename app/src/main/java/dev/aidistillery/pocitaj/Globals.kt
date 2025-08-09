package dev.aidistillery.pocitaj

import android.content.Context
import androidx.room.Room
import dev.aidistillery.pocitaj.data.ActiveUserManager
import dev.aidistillery.pocitaj.data.AdaptiveExerciseSource
import dev.aidistillery.pocitaj.data.AppDatabase
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserDao

interface Globals {
    val userDao: UserDao
    val factMasteryDao: FactMasteryDao
    val exerciseAttemptDao: ExerciseAttemptDao
    val activeUserManager: ActiveUserManager
    val exerciseSource: ExerciseSource
    val inkModelManager: InkModelManager
    val activeUser: User
}

class ProductionGlobals(private val context: Context) : Globals {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "pocitaj-db").build()
    }
    override val userDao: UserDao by lazy { database.userDao() }
    override val factMasteryDao: FactMasteryDao by lazy { database.factMasteryDao() }
    override val exerciseAttemptDao: ExerciseAttemptDao by lazy { database.exerciseAttemptDao() }
    override val activeUserManager: ActiveUserManager by lazy {
        ActiveUserManager(context, userDao)
    }
    override val exerciseSource: ExerciseSource by lazy {
        AdaptiveExerciseSource(factMasteryDao, exerciseAttemptDao, activeUser.id)
    }
    override val inkModelManager: InkModelManager by lazy {
        ModelManager()
    }
    override val activeUser: User
        get() = activeUserManager.activeUser
}
