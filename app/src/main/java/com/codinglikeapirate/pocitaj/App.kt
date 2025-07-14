package com.codinglikeapirate.pocitaj

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.codinglikeapirate.pocitaj.data.AppDatabase
import com.codinglikeapirate.pocitaj.data.ExerciseSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class App : Application() {
    lateinit var inkModelManager: InkModelManager
    val isInkModelManagerInitialized: Boolean
        get() = ::inkModelManager.isInitialized

    lateinit var exerciseSource: ExerciseSource
    val isExerciseSourceInitialized: Boolean
        get() = ::exerciseSource.isInitialized

    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocitaj-db"
        ).build()
        Log.e("App", "onCreate - Created database: $database")
        CoroutineScope(Dispatchers.IO).launch {
            database.userDao().insert(com.codinglikeapirate.pocitaj.data.User(id = 1, name = "Default User"))
        }
    }
}
