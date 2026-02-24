package dev.aidistillery.pocitaj.ui.history

import app.cash.turbine.test
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.data.User
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class HistoryViewModelTest {

    @Test
    fun `uiState combines activity, selection, and filtered list correctly`() = runTest {
        val dao = mockk<ExerciseAttemptDao>()
        val user = User(id = 1, name = "Test")
        val today = LocalDate.now()

        val dailyCounts = listOf(
            dev.aidistillery.pocitaj.data.DailyActivityCount(today.toString(), 2)
        )
        val todayAttempt = createAttempt(123456789L)

        every { dao.getDailyActivityCounts(1) } returns flowOf(dailyCounts)
        every { dao.getAttemptsForDate(1, today.toString()) } returns flowOf(listOf(todayAttempt))

        val viewModel = HistoryViewModel(dao, user)

        viewModel.uiState.test {
            // Initial state is emitted immediately
            awaitItem()

            // Wait for the combined flow to emit the actual data
            val state = awaitItem()

            // 2 total attempts today (mocked by dailyCounts)
            state.todaysCount shouldBe 2

            // Streak should be 1 (because activity map only has today)
            state.currentStreak shouldBe 1

            // Highlight should be Perfect Precision (100% correct, but count is low, but the threshold is 0 for testing sometimes, wait, count < 10 for SpeedyPaws, but PerfectPrecision needs exactly 100% of WHATEVER batch size if we follow the strict analyzer, wait no, analyzer needs 10 for PP too. Let's just check it's empty or has PP based on actual analyzer logic for 1 attempt)
            // Actually, for 1 attempt, no highlights should be generated because all thresholds are >= 10.
            state.todaysHighlights.isEmpty() shouldBe true
        }
    }

    private fun createAttempt(timestamp: Long): ExerciseAttempt {
        return ExerciseAttempt(
            userId = 1,
            timestamp = timestamp,
            problemText = "1+1",
            logicalOperation = Operation.ADDITION,
            correctAnswer = 2,
            submittedAnswer = 2,
            wasCorrect = true,
            durationMs = 1000
        )
    }
}
