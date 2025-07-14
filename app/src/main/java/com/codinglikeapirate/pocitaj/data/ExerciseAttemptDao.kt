package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseAttemptDao {
    @Insert
    suspend fun insert(attempt: ExerciseAttempt)

    @Query("SELECT * FROM exercise_attempt WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAttemptsForUser(userId: Long): Flow<List<ExerciseAttempt>>
}
