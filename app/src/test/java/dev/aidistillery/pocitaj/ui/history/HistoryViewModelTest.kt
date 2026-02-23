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
        val yesterday = today.minusDays(1)

        val dailyCounts = listOf(
            dev.aidistillery.pocitaj.data.DailyActivityCount(today.toString(), 2)
        )
        val todayAttempt = createAttempt(123456789L)
        val yesterdayAttempt = createAttempt(987654321L)

        every { dao.getDailyActivityCounts(1) } returns flowOf(dailyCounts)
        every { dao.getAttemptsForDate(1, today.toString()) } returns flowOf(listOf(todayAttempt))
        every { dao.getAttemptsForDate(1, yesterday.toString()) } returns flowOf(
            listOf(
                yesterdayAttempt
            )
        )

        val viewModel = HistoryViewModel(dao, user)

        viewModel.uiState.test {
            // Wait for initial state with data
            var state = awaitItem()
            while (state.dailyActivity.isEmpty() || state.filteredHistory.isEmpty()) {
                state = awaitItem()
            }
            state.selectedDate shouldBe today
            state.dailyActivity[today] shouldBe 2
            state.filteredHistory shouldBe listOf(todayAttempt)

            // Selection change
            viewModel.selectDate(yesterday)

            // Wait for state with yesterday's data
            state = awaitItem()
            while (state.selectedDate != yesterday || state.filteredHistory != listOf(
                    yesterdayAttempt
                )
            ) {
                state = awaitItem()
            }

            state.selectedDate shouldBe yesterday
            state.filteredHistory shouldBe listOf(yesterdayAttempt)
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
