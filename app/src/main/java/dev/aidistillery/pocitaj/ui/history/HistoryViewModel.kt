package dev.aidistillery.pocitaj.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HistoryUiState(
    val dailyActivity: Map<LocalDate, Int> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),
    val filteredHistory: List<ExerciseAttempt> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUser: User
) : ViewModel() {

    internal val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HistoryUiState> = kotlinx.coroutines.flow.combine(
        exerciseAttemptDao.getDailyActivityCounts(activeUser.id)
            .map { list -> list.associate { LocalDate.parse(it.dateString) to it.count } },
        selectedDate,
        selectedDate.flatMapLatest { date ->
            exerciseAttemptDao.getAttemptsForDate(activeUser.id, date.toString())
        }
    ) { activity, date, history ->
        HistoryUiState(activity, date, history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }
}

/**
 * Formats an ExerciseAttempt into a display-ready string for the history screen.
 * It correctly handles different equation types, including those with missing operands.
 */
fun ExerciseAttempt.toHistoryString(): String {
    return problemText.replace("?", submittedAnswer.toString())
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
