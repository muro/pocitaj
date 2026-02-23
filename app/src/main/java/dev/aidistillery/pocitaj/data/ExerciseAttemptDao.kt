package dev.aidistillery.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DailyActivityCount(
    val dateString: String,
    val count: Int
)

@Dao
interface ExerciseAttemptDao {
    @Insert
    suspend fun insert(attempt: ExerciseAttempt)

    @Query("SELECT * FROM exercise_attempt WHERE userId = :userId AND date(timestamp / 1000, 'unixepoch', 'localtime') = :dateString ORDER BY timestamp DESC")
    fun getAttemptsForDate(userId: Long, dateString: String): Flow<List<ExerciseAttempt>>

    @Query("SELECT date(timestamp / 1000, 'unixepoch', 'localtime') as dateString, COUNT(*) as count FROM exercise_attempt WHERE userId = :userId GROUP BY dateString")
    fun getDailyActivityCounts(userId: Long): Flow<List<DailyActivityCount>>

    @Query("SELECT COUNT(*) FROM exercise_attempt WHERE userId = :userId")
    suspend fun getAttemptCountForUser(userId: Long): Int
}
