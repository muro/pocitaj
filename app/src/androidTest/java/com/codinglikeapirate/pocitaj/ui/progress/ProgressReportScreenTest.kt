package com.codinglikeapirate.pocitaj.ui.progress

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codinglikeapirate.pocitaj.BaseExerciseUiTest
import com.codinglikeapirate.pocitaj.TestApp
import com.codinglikeapirate.pocitaj.data.FactMastery
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressReportScreenTest : BaseExerciseUiTest() {

    @Test
    fun progressReportScreen_displaysHeatmaps() {
        // GIVEN: A set of facts in the database
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val factMasteryDao = application.database.factMasteryDao()
        runBlocking {
            factMasteryDao.upsert(FactMastery("addition-1-1", 1, 5, 0))
            factMasteryDao.upsert(FactMastery("addition-1-2", 1, 3, 0))
            factMasteryDao.upsert(FactMastery("subtraction-2-1", 1, 1, 0))
            factMasteryDao.upsert(FactMastery("subtraction-2-2", 1, 4, 0))
        }

        // WHEN: The user navigates to the progress report screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Progress").performClick()
        composeTestRule.waitForIdle()

        // THEN: The heatmaps should be displayed
        composeTestRule.onNodeWithText("Addition").assertExists()
        composeTestRule.onNodeWithText("Subtraction").assertExists()
    }
}
