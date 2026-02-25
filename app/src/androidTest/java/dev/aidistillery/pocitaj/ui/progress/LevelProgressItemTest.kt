package dev.aidistillery.pocitaj.ui.progress

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LevelProgressItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun levelProgressItem_expandsToShowWeakFactsAndOverflow() {
        val levelId = Curriculum.SumsUpTo10.id

        // Create 8 weak facts belonging to SumsUpTo10 (sum 6..10)
        val testFacts = (3..10).map { part ->
            val sum = 10 // Fixed sum of 10 matches SumsUpTo10 range [6, 10]
            val op2 = sum - part
            FactProgress(
                factId = "$part + $op2 = ?",
                mastery = FactMastery("$part + $op2 = ?", 1, levelId, 1, 0),
                speedBadge = SpeedBadge.NONE
            )
        }

        composeTestRule.setContent {
            AppTheme {
                LevelProgressItem(
                    levelId = levelId,
                    progress = LevelProgress(0.5f, false), // 50% so they are evaluated
                    factProgress = testFacts
                )
            }
        }

        // Initially hidden
        composeTestRule.onNodeWithTag("weak_fact_3 + 7 = ?").assertDoesNotExist()

        // Expand
        composeTestRule.onNodeWithTag("level_row_$levelId").performClick()
        composeTestRule.waitForIdle()

        // Give animation time to play
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()

        // Check overflow chip "+3"
        // Why +3? Because 8 weak facts total. It will show 5 facts and 1 chip that says "+3".
        composeTestRule.onNodeWithTag("weak_fact_overflow", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("+3", useUnmergedTree = true).assertIsDisplayed()
    }
}
