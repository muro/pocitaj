package dev.aidistillery.pocitaj

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.exercise.ResultStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseTestHelperTest : BaseExerciseUiTest() {

    private lateinit var exerciseTestHelper: ExerciseTestHelper

    @Before
    override fun setup() {
        super.setup()
        exerciseTestHelper = ExerciseTestHelper(composeTestRule.activity)
    }

    @Test
    fun testExerciseFlow() = runTest {
        val level = Curriculum.SumsUpTo5
        val exercises = listOf(
            Exercise(Addition(1, 2)),
            Exercise(Addition(3, 4)),
            Exercise(Addition(5, 0))
        )

        // Start the exercise
        exerciseTestHelper.startExercise(level, exercises)
        composeTestRule.awaitIdle()

        // Check the first problem
        assertEquals("1 + 2 = ?", exerciseTestHelper.getExerciseProblem())

        // Submit a correct answer
        exerciseTestHelper.submitAnswer("3")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()


        // Check the second problem
        assertEquals("3 + 4 = ?", exerciseTestHelper.getExerciseProblem())

        // Submit an incorrect answer
        exerciseTestHelper.submitAnswer("8")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()


        // Check the third problem
        assertEquals("5 + 0 = ?", exerciseTestHelper.getExerciseProblem())

        // Submit a correct answer
        exerciseTestHelper.submitAnswer("5")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()

        // No more exercises
        assertNull(exerciseTestHelper.getExerciseProblem())

        // Check the results
        val results = exerciseTestHelper.getResults()
        assertEquals(3, results.size)
        assertEquals(ResultStatus.CORRECT, results[0].status)
        assertEquals(ResultStatus.INCORRECT, results[1].status)
        assertEquals(ResultStatus.CORRECT, results[2].status)
    }
}
