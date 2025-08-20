package dev.aidistillery.pocitaj.ui.history

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUser: User
) : ViewModel() {

    private val _historyByDate = MutableStateFlow<Map<String, List<ExerciseAttempt>>>(emptyMap())
    val historyByDate: StateFlow<Map<String, List<ExerciseAttempt>>> = _historyByDate.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseAttemptDao.getAttemptsForUser(activeUser.id).collect { history ->
                _historyByDate.value = history.groupBy {
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    sdf.format(Date(it.timestamp))
                }
            }
        }
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
