package dev.aidistillery.pocitaj.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, ExerciseAttempt::class, FactMastery::class], version = 3)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun exerciseAttemptDao(): ExerciseAttemptDao
    abstract fun factMasteryDao(): FactMasteryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new table
                db.execSQL(
                    """
                    CREATE TABLE users_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        iconId TEXT NOT NULL DEFAULT 'alligator',
                        color INTEGER NOT NULL DEFAULT ${0xFFF44336.toInt()}
                    )
                """
                )

                // Copy the data
                db.execSQL(
                    """
                    INSERT INTO users_new (id, name)
                    SELECT id, name FROM user
                """
                )

                // Remove the old table
                db.execSQL("DROP TABLE user")

                // Rename the new table
                db.execSQL("ALTER TABLE users_new RENAME TO user")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fact_mastery ADD COLUMN level TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE TABLE fact_mastery_new (factId TEXT NOT NULL, userId INTEGER NOT NULL, level TEXT NOT NULL, strength INTEGER NOT NULL, lastTestedTimestamp INTEGER NOT NULL, avgDurationMs INTEGER NOT NULL, PRIMARY KEY(factId, userId, level), FOREIGN KEY(userId) REFERENCES user(id) ON DELETE CASCADE)")
                db.execSQL("INSERT INTO fact_mastery_new (factId, userId, level, strength, lastTestedTimestamp, avgDurationMs) SELECT factId, userId, level, strength, lastTestedTimestamp, avgDurationMs FROM fact_mastery")
                db.execSQL("DROP TABLE fact_mastery")
                db.execSQL("ALTER TABLE fact_mastery_new RENAME TO fact_mastery")
                db.execSQL("CREATE INDEX index_fact_mastery_userId_strength_lastTestedTimestamp ON fact_mastery (userId, strength, lastTestedTimestamp)")
            }
        }
    }


    /**
     * Type converters to allow Room to store custom types.
     */
    class Converters {
        @TypeConverter
        fun fromOperation(operation: Operation): String {
            return operation.name
        }

        @TypeConverter
        fun toOperation(name: String): Operation {
            return Operation.valueOf(name)
        }
    }
}
