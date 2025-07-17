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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// TODO: this class is not useful, it duplicates the factId
data class FactProgress(
    val factId: String,
    val mastery: FactMastery?
)

data class OperationProgress(val progress: Float, val isMastered: Boolean)

class ProgressReportViewModel(
    factMasteryDao: FactMasteryDao
) : ViewModel() {

    val progressByOperation: StateFlow<Map<Operation, List<FactProgress>>> =
        factMasteryDao.getAllFactsForUser(1) // Assuming user ID 1
            .map { masteryList ->
                val masteryMap = masteryList.associateBy { it.factId }
                val allFactsByOperation = Curriculum.getAllLevels()
                    .flatMap { level ->
                        level.getAllPossibleFactIds().map { factId ->
                            level.operation to factId
                        }
                    }
                    .groupBy({ it.first }) { it.second }
                    .mapValues { (_, factIds) -> factIds.distinct() }

                allFactsByOperation.mapValues { (_, factIds) ->
                    factIds.map { factId ->
                        FactProgress(factId, masteryMap[factId])
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    val operationProgress: StateFlow<Map<Operation, OperationProgress>> =
        progressByOperation.map { progressMap ->
            progressMap.mapValues { (_, facts) ->
                val masteredCount = facts.count { (it.mastery?.strength ?: 0) >= 5 }
                val progress = if (facts.isNotEmpty()) masteredCount.toFloat() / facts.size else 0f
                OperationProgress(progress, progress >= 1.0f)
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