package dev.aidistillery.pocitaj

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.data.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        var db = helper.createDatabase(TEST_DB, 3).apply {
            // Seed with legacy data
            LegacyDataSeeder.seed(this)
            close()
        }

        // Run migration
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        // Validation - Check specific facts
        val cursor = db.query("SELECT * FROM fact_mastery WHERE factId = ?", arrayOf("3 + 5 = ?"))
        assert(cursor.moveToFirst()) { "Fact 3 + 5 = ? not found after migration" }

        // specific check for data integrity from LegacyDataSeeder
        // Legacy: ADDITION_3_5 -> Strength 3
        val strength = cursor.getInt(cursor.getColumnIndex("strength"))
        assert(strength == 3) { "Strength mismatch for 3 + 5 = ?. Expected 3, got $strength" }
        cursor.close()

        // Check for general mastery (empty level) migration
        // Legacy: ADDITION_2_2 with level "" -> 2 + 2 = ? with level ""
        val cursorGlobal = db.query(
            "SELECT * FROM fact_mastery WHERE factId = ? AND level = ?",
            arrayOf("2 + 2 = ?", "")
        )
        assert(cursorGlobal.moveToFirst()) { "Global mastery for 2 + 2 = ? not found" }
        val globalStrength = cursorGlobal.getInt(cursorGlobal.getColumnIndex("strength"))
        assert(globalStrength == 2) { "Global strength mismatch. Expected 2, got $globalStrength" }
        cursorGlobal.close()

        // Validate Missing Operand conversion: ADDITION_3_?_10 -> 3 + ? = 10
        val cursorMissing =
            db.query("SELECT * FROM fact_mastery WHERE factId = ?", arrayOf("3 + ? = 10"))
        assert(cursorMissing.moveToFirst()) { "Missing addend 3 + ? = 10 not found after migration" }
        val missingStrength = cursorMissing.getInt(cursorMissing.getColumnIndex("strength"))
        assert(missingStrength == 4) { "Strength mismatch for 3 + ? = 10. Expected 4, got $missingStrength" }
        cursorMissing.close()
    }
}
