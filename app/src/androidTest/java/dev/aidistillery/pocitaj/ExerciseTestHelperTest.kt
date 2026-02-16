package dev.aidistillery.pocitaj

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.aidistillery.pocitaj.logic.Addition
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Exercise
import dev.aidistillery.pocitaj.ui.exercise.ResultStatus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
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
        exerciseTestHelper.getExerciseProblem() shouldBe "1 + 2 = ?"

        // Submit a correct answer
        exerciseTestHelper.submitAnswer("3")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()


        // Check the second problem
        exerciseTestHelper.getExerciseProblem() shouldBe "3 + 4 = ?"

        // Submit an incorrect answer
        exerciseTestHelper.submitAnswer("8")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()


        // Check the third problem
        exerciseTestHelper.getExerciseProblem() shouldBe "5 + 0 = ?"

        // Submit a correct answer
        exerciseTestHelper.submitAnswer("5")
        composeTestRule.awaitIdle()
        exerciseTestHelper.onFeedbackAnimationFinished()
        composeTestRule.awaitIdle()

        // No more exercises
        exerciseTestHelper.getExerciseProblem().shouldBeNull()

        // Check the results
        val results = exerciseTestHelper.getResults()
        results.size shouldBe 3
        results[0].status shouldBe ResultStatus.CORRECT
        results[1].status shouldBe ResultStatus.INCORRECT
        results[2].status shouldBe ResultStatus.CORRECT
    }
}
