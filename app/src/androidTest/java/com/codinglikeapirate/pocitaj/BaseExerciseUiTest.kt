package com.codinglikeapirate.pocitaj

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule

abstract class BaseExerciseUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ExerciseBookActivity>()

    // Model Handling Note:
    // Currently, we are letting the app attempt to download the Digital Ink Recognition model
    // if it's not already present. If tests become flaky due to network issues or download times,
    // we will need to introduce a mechanism to mock or pre-prime the ModelManager
    // (e.g., by using a fake/test version or ensuring the model is downloaded before tests run).

    @Before
    fun waitForAppToBeReady() {
        // Wait for the loading screen to disappear and setup screen to be visible.
        // Look for a button that uniquely identifies the ExerciseSetupScreen.
        // The text "Start Addition" is on one of the buttons.
        // Allow up to 30 seconds for the model to download, though it should be faster.
        var attempts = 0
        val maxAttempts = 30 // 30 attempts * 1000ms = 30 seconds
        val delayMillis = 1000L
        var setupScreenVisible = false

        while (attempts < maxAttempts && !setupScreenVisible) {
            try {
                composeTestRule.onNodeWithText("Start Addition").assertIsDisplayed()
                setupScreenVisible = true
            } catch (e: Exception) { // More specific: NoMatchingNodeException or AssertionError
                attempts++
                Thread.sleep(delayMillis)
            }
        }

        if (!setupScreenVisible) {
            throw AssertionError("ExerciseSetupScreen (with 'Start Addition' button) did not become visible after ${maxAttempts * delayMillis / 1000} seconds.")
        }
    }

    fun navigateToExerciseType(exerciseTypeButtonText: String) {
        // Click on the button to start the specific exercise type
        composeTestRule.onNodeWithText(exerciseTypeButtonText)
            //.assertIsDisplayed() // Optional: performClick will fail if not displayed
            .performClick()

        // Wait for the ExerciseScreen to be loaded by checking for a unique element.
        // The placeholder text "Recognized Text: ???" is part of the ExerciseScreen.
        // Allow up to 5 seconds for the screen transition.
        var attempts = 0
        val maxAttempts = 10 // 10 attempts * 500ms = 5 seconds
        val delayMillis = 500L
        var exerciseScreenVisible = false

        while (attempts < maxAttempts && !exerciseScreenVisible) {
            try {
                composeTestRule.onNodeWithText("Recognized Text: ???").assertIsDisplayed()
                exerciseScreenVisible = true
            } catch (e: Exception) { // More specific: NoMatchingNodeException or AssertionError
                attempts++
                Thread.sleep(delayMillis)
            }
        }

        if (!exerciseScreenVisible) {
            throw AssertionError("ExerciseScreen (with 'Recognized Text: ???') did not become visible after ${maxAttempts * delayMillis / 1000} seconds.")
        }
    }
}
