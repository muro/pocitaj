package com.codinglikeapirate.pocitaj.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_attempt",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class ExerciseAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val timestamp: Long,
    val problemText: String,
    val logicalOperation: Operation,
    val correctAnswer: Int,
    val submittedAnswer: Int,
    val wasCorrect: Boolean,
    val durationMs: Long
)
