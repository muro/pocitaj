package com.codinglikeapirate.pocitaj

import com.codinglikeapirate.pocitaj.logic.Addition
import com.codinglikeapirate.pocitaj.logic.Exercise
import org.junit.Test

class FirstLaunchTest : BaseExerciseUiTest() {

    @Test
    fun whenNoUserExists_completingExercise_doesNotCrash() {
        // 1. Set a dummy exercise
        setExercises(listOf(Exercise(Addition(1, 1))))

        // 2. Navigate to the exercise screen
        navigateToOperation("+")

        // 3. Draw an answer
        drawAnswer("2")

        // 4. Verify that the app does not crash and that the correct feedback is shown
        verifyFeedback(FeedbackType.CORRECT)
    }
}
