package com.codinglikeapirate.pocitaj.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "fact_mastery",
    primaryKeys = ["factId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "strength", "lastTestedTimestamp"])]
)
data class FactMastery(
    val factId: String,
    val userId: Long,
    val strength: Int,
    val lastTestedTimestamp: Long
)
