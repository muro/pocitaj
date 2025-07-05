package com.codinglikeapirate.pocitaj.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [User::class, ExerciseAttempt::class, FactMastery::class], version = 1)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // We will add abstract DAO functions here in the next step.

    /**
     * Type converters to allow Room to store custom types.
     */
    class Converters {
        @TypeConverter
        fun fromOperation(operation: Operation): String {
            return operation.name
        }

        @TypeConverter
        fun toOperation(name: String): Operation {
            return Operation.valueOf(name)
        }
    }
}
