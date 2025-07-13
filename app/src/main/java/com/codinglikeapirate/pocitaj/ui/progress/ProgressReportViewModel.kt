package com.codinglikeapirate.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
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

    private val _operationProgress = MutableStateFlow<Map<Operation, OperationProgress>>(emptyMap())
    val operationProgress: StateFlow<Map<Operation, OperationProgress>> = _operationProgress.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val masteredFacts = factMasteryDao.getAllFactsForUser(1).associateBy { it.factId }
            val allLevels = Curriculum.getAllLevels()

            // Calculate level-specific progress
            val levelsToDisplay = allLevels.filter { level ->
                allLevels.none { otherLevel ->
                    level != otherLevel &&
                            level.operation == otherLevel.operation &&
                            otherLevel.getAllPossibleFactIds().size > level.getAllPossibleFactIds().size &&
                            otherLevel.getAllPossibleFactIds().toSet().containsAll(level.getAllPossibleFactIds().toSet())
                }
            }
            val progressByLevelMap = mutableMapOf<Level, List<FactProgress>>()
            for (level in levelsToDisplay) {
                val allFactIdsForLevel = level.getAllPossibleFactIds()
                val progressForLevel = allFactIdsForLevel.map { factId ->
                    FactProgress(factId, masteredFacts[factId])
                }
                if (progressForLevel.isNotEmpty()) {
                    progressByLevelMap[level] = progressForLevel
                }
            }
            _progressByLevel.value = progressByLevelMap

            // Calculate operation-specific progress
            val operationProgressMap = mutableMapOf<Operation, OperationProgress>()
            for (operation in Operation.entries) {
                val levelsForOperation = allLevels.filter { it.operation == operation }
                if (levelsForOperation.isEmpty()) continue

                val allFactsForOperation = levelsForOperation.flatMap { it.getAllPossibleFactIds() }.toSet()
                val masteredFactsForOperation = masteredFacts.values.filter { it.factId.startsWith(operation.name) }.count { it.strength >= 5 }

                val progress = if (allFactsForOperation.isNotEmpty()) {
                    masteredFactsForOperation.toFloat() / allFactsForOperation.size
                } else {
                    0f
                }
                val isMastered = progress >= 1.0f

                operationProgressMap[operation] = OperationProgress(progress, isMastered)
            }
            _operationProgress.value = operationProgressMap
        }
    }
}

data class OperationProgress(val progress: Float, val isMastered: Boolean)

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