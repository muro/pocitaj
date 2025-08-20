package dev.aidistillery.pocitaj.ui.history

import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.Operation
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryViewModelTest {

    @Test
    fun `toHistoryString formats standard equation correctly`() {
        val attempt = ExerciseAttempt(
            problemText = "5 + 5 = ?",
            submittedAnswer = 10,
            wasCorrect = true,
            correctAnswer = 10,
            durationMs = 1000,
            timestamp = 0,
            userId = 1,
            logicalOperation = Operation.ADDITION
        )
        val expected = "5 + 5 = 10"
        val actual = attempt.toHistoryString()
        assertEquals(expected, actual)
    }

    @Test
    fun `toHistoryString formats missing addend equation correctly`() {
        val attempt = ExerciseAttempt(
            problemText = "2 + ? = 7",
            submittedAnswer = 5,
            wasCorrect = true,
            correctAnswer = 5,
            durationMs = 1000,
            timestamp = 0,
            userId = 1,
            logicalOperation = Operation.ADDITION
        )
        val expected = "2 + 5 = 7"
        val actual = attempt.toHistoryString()
        assertEquals(expected, actual)
    }

    @Test
    fun `toHistoryString handles incorrect answer`() {
        val attempt = ExerciseAttempt(
            problemText = "10 - 3 = ?",
            submittedAnswer = 6,
            wasCorrect = false,
            correctAnswer = 7,
            durationMs = 1200,
            timestamp = 0,
            userId = 1,
            logicalOperation = Operation.SUBTRACTION
        )
        val expected = "10 - 3 = 6"
        val actual = attempt.toHistoryString()
        assertEquals(expected, actual)
    }
}
