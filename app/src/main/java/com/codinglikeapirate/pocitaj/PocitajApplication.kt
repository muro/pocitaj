package com.codinglikeapirate.pocitaj

import android.app.Application
import androidx.room.Room
import com.codinglikeapirate.pocitaj.data.AppDatabase

open class PocitajApplication : Application() {
    open val inkModelManager: InkModelManager by lazy {
        ModelManager()
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pocitaj-db"
        ).build()
    }
}
