package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseAttemptDao {
    @Insert
    suspend fun insert(attempt: ExerciseAttempt)

    @Query("SELECT * FROM exercise_attempt WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAttemptsForUser(userId: Long): List<ExerciseAttempt>
}
