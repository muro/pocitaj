package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Level
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.math.ceil

class LevelCompletionTest : AdaptiveExerciseUiTest() {
    @Test
    fun whenLevelCompletedCorrectly_thenProgressIncreases() {
        openOperationCard("×")
        val level = Curriculum.MultiplicationTableLevel(3)
        val levelId = level.id
        composeTestRule.onNodeWithTag("level_tile_${levelId}").assertIsDisplayed()
        assertEquals(0, getProgress(levelId))

        answerAllQuestionsCorrectly("×", levelId)

        openOperationCard("×")
        val progress = getProgress(levelId)
        assertTrue("Progress should be greater than 0, but was $progress", progress > 0)
        composeTestRule.onNodeWithTag("level_tile_${levelId}").assertIsDisplayed()
    }

    @Test
    fun whenProgressCrossesStarThreshold_thenNewStarIsAwarded() {
        // SETUP: Define the level we're testing
        val level = Curriculum.MultiplicationTableLevel(4)
        val levelId = level.id

        // ARRANGE: Programmatically set mastery to just below the 1-star threshold (59%)
        setMasteryProgress(level, 0.59f)

        // ACT 1: Open the screen and verify the initial state is correct
        openOperationCard("×")
        composeTestRule.onNodeWithTag("level_tile_${levelId}").assertIsDisplayed()
        val initialProgress = getProgress(levelId)
        assertEquals("Pre-condition failed: Initial progress should be 59%", 59, initialProgress)

        // ACT 2: Play one full session, answering all questions correctly
        answerAllQuestionsCorrectly("×", levelId)

        // ASSERT: Verify the final state has crossed the threshold
        openOperationCard("×") // Re-open the card to refresh the UI with final progress
        composeTestRule.onNodeWithTag("level_tile_${levelId}").assertIsDisplayed()
        val finalProgress = getProgress(levelId)
        assertTrue("Post-condition failed: Final progress should be > 60%", finalProgress > 60)

        // TODO: Once the "New Star" celebration animation/UI is implemented,
        // add an assertion here to verify its visibility. For example:
        // composeTestRule.onNodeWithTag("new_star_animation").assertIsDisplayed()
    }

    /**
     * Helper function to programmatically set the mastery progress for a given level
     * by directly manipulating the FactMasteryDao.
     */
    private fun setMasteryProgress(level: Level, progress: Float) {
        runBlocking {
            val facts = level.getAllPossibleFactIds()
            // First, clear any existing mastery for this level to start fresh
            for (factId in facts) {
                globals.factMasteryDao.upsert(
                    FactMastery(factId = factId, userId = 1, strength = 0, level = "", lastTestedTimestamp = 100)
                )
            }

            // Calculate the target total strength based on the desired progress
            val maxStrength = facts.size * 5
            val targetStrength = ceil(maxStrength * progress).toInt()

            // Distribute this strength among the facts
            val factsToFullyMaster = targetStrength / 5
            val remainingStrength = targetStrength % 5

            for (i in 0 until factsToFullyMaster) {
                globals.factMasteryDao.upsert(
                    FactMastery(factId = facts[i], userId = 1, strength = 5, level = "", lastTestedTimestamp = 100)
                )
            }
            if (remainingStrength > 0 && factsToFullyMaster < facts.size) {
                globals.factMasteryDao.upsert(
                    FactMastery(factId = facts[factsToFullyMaster], userId = 1, strength = remainingStrength, level = "", lastTestedTimestamp = 100)
                )
            }
        }
    }
}