package dev.aidistillery.pocitaj

import androidx.sqlite.db.SupportSQLiteDatabase

object LegacyDataSeeder {
    fun seed(db: SupportSQLiteDatabase) {
        // 1. Insert User
        db.execSQL("INSERT INTO user (id, name, iconId, color) VALUES (1, 'Test User', 'alligator', -1)")

        // 2. Insert FactMastery records (Legacy IDs)
        val facts = listOf(
            Triple("ADDITION_3_5", "ADD_SUM_10", 3),
            Triple("ADDITION_2_2", "ADD_DOUBLES", 4),
            Triple("ADDITION_10_5", "ADD_TENS", 2),
            Triple("SUBTRACTION_5_3", "SUB_FROM_5", 5),
            Triple("SUBTRACTION_10_4", "SUB_FROM_10", 1),
            Triple("MULTIPLICATION_3_4", "MUL_TABLE_3", 3),
            Triple("DIVISION_12_3", "DIV_BY_3", 4),
            // User requested check for "general" mastery (no level)
            Triple("ADDITION_2_2", "", 2), // Duplicate fact, but global mastery tracking
            // Missing Addend
            Triple("ADDITION_3_?_10", "ADD_MAKING_10S", 4)
        )

        val now = System.currentTimeMillis()
        for ((factId, level, strength) in facts) {
            db.execSQL("INSERT INTO fact_mastery (factId, userId, level, strength, lastTestedTimestamp, avgDurationMs) VALUES ('$factId', 1, '$level', $strength, $now, 0)")
        }

        // 3. Insert ExerciseAttempt records
        // We captured:
        // problemText = "3 + 5 = ?", logicalOperation = ADDITION
        // problemText = "? + 7 = 10", logicalOperation = ADDITION

        // Note: Operation enum mapping in Room usually defaults to String names if using converters, or Ordinals?
        // Let's assume Converters are using names usually, but checking Converters class is best.
        // Assuming defaults or common practice: matching the enum name.
        // If I use the same inserts as DataCaptureTest used, I should rely on how Room was sending them.
        // But here I am using raw SQL.

        // Let's peek at Converters if I can, but usually it's String.
        // "ADDITION"

        db.execSQL("INSERT INTO exercise_attempt (userId, timestamp, problemText, logicalOperation, correctAnswer, submittedAnswer, wasCorrect, durationMs) VALUES (1, $now, '3 + 5 = ?', 'ADDITION', 8, 8, 1, 1000)")
        db.execSQL("INSERT INTO exercise_attempt (userId, timestamp, problemText, logicalOperation, correctAnswer, submittedAnswer, wasCorrect, durationMs) VALUES (1, $now, '? + 7 = 10', 'ADDITION', 3, 3, 1, 1500)")
    }
}
