package dev.aidistillery.pocitaj

import android.content.Context
import androidx.room.Room
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.recognition.Ink
import dev.aidistillery.pocitaj.data.AppDatabase
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.ExerciseSource
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.FakeActiveUserManager
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserDao

object FakeInkModelManager : InkModelManager {
    private var recognitionResult = "123"

    override fun setModel(languageTag: String): String {
        return "fake model set"
    }

    override fun deleteActiveModel(): Task<String?> {
        return Tasks.forResult("fake model deleted")
    }

    override fun download(): Task<String?> {
        return Tasks.forResult("fake model downloaded")
    }

    override suspend fun recognizeInk(ink: Ink, hint: String): String {
        return recognitionResult
    }
}

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

    @Suppress("unused")
    fun reset() {
        activeUserManager.reset()
        database.clearAllTables()
    }
}
