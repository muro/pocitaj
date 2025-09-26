package dev.aidistillery.pocitaj.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FactMasteryDao {
    @Query("SELECT * FROM fact_mastery WHERE userId = :userId")
    fun getAllFactsForUser(userId: Long): Flow<List<FactMastery>>

    @Query("SELECT * FROM fact_mastery WHERE factId = :factId AND userId = :userId AND level = :level")
    suspend fun getFactMastery(userId: Long, factId: String, level: String): FactMastery?

    @Query("SELECT * FROM fact_mastery WHERE factId = :factId AND userId = :userId AND level = ''")
    suspend fun getFactMastery(userId: Long, factId: String): FactMastery?

    @Upsert
    suspend fun upsert(factMastery: FactMastery)

    suspend fun upsert(factMastery: FactMastery, level: String) {
        upsert(factMastery.copy(level = ""))
        upsert(factMastery.copy(level = level))
    }
}
