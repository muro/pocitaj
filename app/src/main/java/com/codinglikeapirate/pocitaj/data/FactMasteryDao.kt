package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface FactMasteryDao {
    @Upsert
    suspend fun upsert(factMastery: FactMastery)
}
