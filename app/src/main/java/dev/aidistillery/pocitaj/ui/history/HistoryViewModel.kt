package dev.aidistillery.pocitaj.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.logic.ActivityAnalyzer
import dev.aidistillery.pocitaj.logic.SmartHighlight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HistoryUiState(
    val currentStreak: Int = 0,
    val todaysCount: Int = 0,
    val todaysHighlights: List<SmartHighlight> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUser: User
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = kotlinx.coroutines.flow.combine(
        exerciseAttemptDao.getDailyActivityCounts(activeUser.id)
            .map { list -> list.associate { LocalDate.parse(it.dateString) to it.count } },
        exerciseAttemptDao.getAttemptsForDate(activeUser.id, LocalDate.now().toString())
    ) { dailyActivity, todaysHistory ->

        val streak = ActivityAnalyzer.calculateStreak(dailyActivity, LocalDate.now())
        val count = dailyActivity.getOrDefault(LocalDate.now(), 0)
        val highlights = ActivityAnalyzer.generateHighlights(todaysHistory)

        HistoryUiState(streak, count, highlights)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

}


object HistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val globals = App.app.globals
        return HistoryViewModel(
            exerciseAttemptDao = globals.exerciseAttemptDao,
            activeUser = globals.activeUser
        ) as T
    }
}
