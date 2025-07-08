package com.codinglikeapirate.pocitaj

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinglikeapirate.pocitaj.data.AppDatabase

open class PocitajApplication : Application() {
    open val inkModelManager: InkModelManager by lazy {
        ModelManager()
    }

    open val exerciseBook: ExerciseBook by lazy {
        ExerciseBook()
    }

    val database: AppDatabase by lazy {
        Log.e("PocitajApplication", "database - lazy")
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocitaj-db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate the database with a default user using raw SQL
                // to avoid the circular dependency on the 'database' lazy property.
                db.execSQL("INSERT INTO user (id, name) VALUES (1, 'Default User')")
            }
        })
            .build()
    }
}
