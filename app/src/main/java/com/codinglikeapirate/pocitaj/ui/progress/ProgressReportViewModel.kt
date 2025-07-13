package com.codinglikeapirate.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Level
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressReportViewModel(
    private val factMasteryDao: FactMasteryDao
) : ViewModel() {

    private val _progressByLevel = MutableStateFlow<Map<Level, List<FactProgress>>>(emptyMap())
    val progressByLevel: StateFlow<Map<Level, List<FactProgress>>> = _progressByLevel.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val masteredFacts = factMasteryDao.getAllFactsForUser(1).associateBy { it.factId }
            val levels = Curriculum.getAllLevels()

            val progressMap = mutableMapOf<Level, List<FactProgress>>()

            for (level in levels) {
                val allFactIdsForLevel = level.getAllPossibleFactIds()
                val progressForLevel = allFactIdsForLevel.map { factId ->
                    FactProgress(factId, masteredFacts[factId])
                }
                if (progressForLevel.isNotEmpty()) {
                    progressMap[level] = progressForLevel
                }
            }
            _progressByLevel.value = progressMap
        }
    }
}

data class FactProgress(val factId: String, val mastery: FactMastery?)

object ProgressReportViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[APPLICATION_KEY] as App
        return ProgressReportViewModel(
            factMasteryDao = application.database.factMasteryDao()
        ) as T
    }
}