package dev.aidistillery.pocitaj.ui.progress

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.aidistillery.pocitaj.BaseExerciseUiTest
import dev.aidistillery.pocitaj.TestApp
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.logic.Curriculum
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressReportScreenTest : BaseExerciseUiTest() {

    @Test
    fun progressReportScreen_displaysLevelProgress() {
        // GIVEN: A set of facts in the database
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val factMasteryDao = application.globals.factMasteryDao
        runBlocking {
            factMasteryDao.upsert(FactMastery("ADDITION_1_1", 1, 5, 0))
            factMasteryDao.upsert(FactMastery("ADDITION_1_2", 1, 3, 0))
            factMasteryDao.upsert(FactMastery("SUBTRACTION_2_1", 1, 1, 0))
            factMasteryDao.upsert(FactMastery("SUBTRACTION_2_2", 1, 4, 0))
            factMasteryDao.upsert(FactMastery("MULTIPLICATION_2_3", 1, 5, 0))
        }

        // WHEN: The user navigates to the progress report screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("My Progress").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().printToLog("ProgressReportScreenTest")

        // THEN: The progress grids for the levels should be displayed
        composeTestRule.onNodeWithTag("operation_card_ADDITION").assertIsDisplayed()
        composeTestRule.onNodeWithTag("progress_report_list")
            .performScrollToNode(hasTestTag("operation_card_SUBTRACTION"))
        composeTestRule.onNodeWithTag("operation_card_SUBTRACTION").assertIsDisplayed()
        composeTestRule.onNodeWithTag("progress_report_list")
            .performScrollToNode(hasTestTag("operation_card_MULTIPLICATION"))
        composeTestRule.onNodeWithTag("operation_card_MULTIPLICATION").assertIsDisplayed()
        composeTestRule.onNodeWithTag("progress_report_list")
            .performScrollToNode(hasTestTag("operation_card_DIVISION"))
        composeTestRule.onNodeWithTag("operation_card_DIVISION").assertIsDisplayed()
    }

    @Test
    fun progressReportScreen_showsCorrectProgress() {
        // GIVEN: A user with partial and full mastery of some levels
        val application =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApp
        val factMasteryDao = application.globals.factMasteryDao
        runBlocking {
            // Master all of SumsUpTo5
            Curriculum.SumsUpTo5.getAllPossibleFactIds().forEach { factId ->
                factMasteryDao.upsert(FactMastery(factId, 1, 5, 0))
            }
            // Master half of SubtractionFrom5
            val subtractionFacts = Curriculum.SubtractionFrom5.getAllPossibleFactIds()
            subtractionFacts.take(subtractionFacts.size / 2).forEach { factId ->
                factMasteryDao.upsert(FactMastery(factId, 1, 5, 0))
            }
        }

        // WHEN: The user navigates to the progress report screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("My Progress").performClick()
        composeTestRule.waitForIdle()

        // THEN: The progress bars should show the correct progress
        composeTestRule.onNodeWithTag("progress_report_list")
            .performScrollToNode(hasTestTag("operation_card_ADDITION"))
        composeTestRule.onNodeWithTag("progress_ADD_SUM_5_1.0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("progress_report_list")
            .performScrollToNode(hasTestTag("operation_card_SUBTRACTION"))
        composeTestRule.onRoot().printToLog("progress_report_list")
        composeTestRule.onNodeWithTag("progress_SUB_FROM_5_0.5").assertIsDisplayed()
    }
}
