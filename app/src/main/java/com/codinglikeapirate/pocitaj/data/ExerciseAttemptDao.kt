package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface ExerciseAttemptDao {
    @Insert
    suspend fun insert(attempt: ExerciseAttempt)
}
