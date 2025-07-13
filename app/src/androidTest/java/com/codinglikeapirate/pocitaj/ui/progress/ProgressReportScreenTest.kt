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
    fun progressReportScreen_displaysLevelProgress() {
        // GIVEN: A set of facts in the database
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val factMasteryDao = application.database.factMasteryDao()
        runBlocking {
            factMasteryDao.upsert(FactMastery("ADDITION_1_1", 1, 5, 0))
            factMasteryDao.upsert(FactMastery("ADDITION_1_2", 1, 3, 0))
            factMasteryDao.upsert(FactMastery("SUBTRACTION_2_1", 1, 1, 0))
            factMasteryDao.upsert(FactMastery("SUBTRACTION_2_2", 1, 4, 0))
        }

        // WHEN: The user navigates to the progress report screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Progress").performClick()
        composeTestRule.waitForIdle()

        // THEN: The progress grids for the levels should be displayed
        composeTestRule.onNodeWithText("ADD_SUM_5").assertExists()
        composeTestRule.onNodeWithText("SUB_FROM_5").assertExists()
    }
}
