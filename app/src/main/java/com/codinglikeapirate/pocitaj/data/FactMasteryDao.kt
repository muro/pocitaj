package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FactMasteryDao {
    @Query("SELECT * FROM fact_mastery WHERE userId = :userId")
    fun getAllFactsForUser(userId: Long): Flow<List<FactMastery>>

    @Query("SELECT * FROM fact_mastery WHERE factId = :factId AND userId = :userId")
    suspend fun getFactMastery(userId: Long, factId: String): FactMastery?

    @Upsert
    suspend fun upsert(factMastery: FactMastery)
}
