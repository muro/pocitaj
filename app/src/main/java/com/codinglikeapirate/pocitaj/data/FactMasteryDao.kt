package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface FactMasteryDao {
    @Upsert
    suspend fun upsert(factMastery: FactMastery)

    @Query("SELECT * FROM fact_mastery WHERE factId = :factId AND userId = :userId")
    suspend fun getFactMastery(userId: Long, factId: String): FactMastery?

    @Query("SELECT * FROM fact_mastery WHERE userId = :userId")
    suspend fun getAllFactsForUser(userId: Long): List<FactMastery>
}
