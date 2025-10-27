package dev.aidistillery.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import dev.aidistillery.pocitaj.logic.Curriculum
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LevelCompletionTest : AdaptiveExerciseUiTest() {
    @Test
    fun whenLevelCompletedCorrectly_thenStarRatingIncreases() {
        openOperationCard("×")
        val levelId = Curriculum.MultiplicationTableLevel(3).id
        composeTestRule.onNodeWithTag("leveltile_${levelId}").assertIsDisplayed()
        assertEquals(getProgress(levelId), 0)

        answerAllQuestionsCorrectly("×", levelId)

        openOperationCard("×")
        assertTrue(getProgress(levelId) > 20)
        composeTestRule.onNodeWithTag("leveltile_${levelId}").assertIsDisplayed()
    }
}