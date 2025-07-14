package com.codinglikeapirate.pocitaj

import android.util.Log
import androidx.room.Room
import com.codinglikeapirate.pocitaj.data.AppDatabase

class TestApp : App() {
    override fun onCreate() {
        super.onCreate()
        inkModelManager = FakeInkModelManager
        Log.e("TestApp", "onCreate - Before creating database: $database")
        database = Room.inMemoryDatabaseBuilder(
            applicationContext,
            AppDatabase::class.java
        ).build()
        Log.e("TestApp", "onCreate - After creating database: $database")
        exerciseSource = ExerciseBook()
    }
}
