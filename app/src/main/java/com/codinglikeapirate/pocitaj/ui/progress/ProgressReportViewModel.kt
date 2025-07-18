package com.codinglikeapirate.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMastery
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Level
import com.codinglikeapirate.pocitaj.ui.progress.OperationProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FactProgress(
    val factId: String,
    val mastery: FactMastery?
)

data class LevelProgress(
    val progress: Float,
    val isMastered: Boolean
)

class ProgressReportViewModel(
    factMasteryDao: FactMasteryDao
) : ViewModel() {

    private val allFactsByOperation = Curriculum.getAllLevels()
        .flatMap { level ->
            level.getAllPossibleFactIds().map { factId ->
                level.operation to factId
            }
        }
        .groupBy({ it.first }) { it.second }
        .mapValues { (_, factIds) -> factIds.distinct() }

    val factProgressByOperation: StateFlow<Map<Operation, List<FactProgress>>> =
        factMasteryDao.getAllFactsForUser(1) // Assuming user ID 1
            .map { masteryList ->
                val masteryMap = masteryList.associateBy { it.factId }
                allFactsByOperation.mapValues { (operation, factIds) ->
                    factIds.map { factId ->
                        FactProgress(factId, masteryMap[factId])
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Operation.entries.associateWith { emptyList() }
            )

    val levelProgressByOperation: StateFlow<Map<Operation, Map<String, LevelProgress>>> =
        factMasteryDao.getAllFactsForUser(1) // Assuming user ID 1
            .map { masteryList ->
                val masteryMap = masteryList.associateBy { it.factId }
                Curriculum.getAllLevels().groupBy { it.operation }
                    .mapValues { (_, levels) ->
                        levels.associate { level ->
                            val levelFacts = level.getAllPossibleFactIds()
                            val masteredCount = levelFacts.count { factId ->
                                (masteryMap[factId]?.strength ?: 0) >= 5
                            }
                            val progress = if (levelFacts.isNotEmpty()) {
                                masteredCount.toFloat() / levelFacts.size
                            } else {
                                0f
                            }
                            level.id to LevelProgress(progress, progress >= 1.0f)
                        }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
}

object ProgressReportViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
        return ProgressReportViewModel(
            factMasteryDao = application.database.factMasteryDao()
        ) as T
    }
}