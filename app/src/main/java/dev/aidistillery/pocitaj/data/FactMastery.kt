package dev.aidistillery.pocitaj.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "fact_mastery",
    primaryKeys = ["factId", "userId", "level"],
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
    val level: String,
    val strength: Int = 0,
    val lastTestedTimestamp: Long,
    val avgDurationMs: Long = 0
) {
    init {
        require(strength in 0..10) { "Strength must be between 0 and 10, but was $strength" }
    }
}
