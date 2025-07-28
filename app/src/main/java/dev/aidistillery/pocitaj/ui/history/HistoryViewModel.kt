package dev.aidistillery.pocitaj.ui.history

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class HistoryViewModel(
    private val exerciseAttemptDao: ExerciseAttemptDao
) : ViewModel() {

    private val _historyByDate = MutableStateFlow<Map<String, List<ExerciseAttempt>>>(emptyMap())
    val historyByDate: StateFlow<Map<String, List<ExerciseAttempt>>> = _historyByDate.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseAttemptDao.getAttemptsForUser(1).collect { history ->
                _historyByDate.value = history.groupBy {
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    sdf.format(Date(it.timestamp))
                }
            }
        }
    }
}

object HistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as App
        return HistoryViewModel(
            exerciseAttemptDao = application.database.exerciseAttemptDao()
        ) as T
    }
}
