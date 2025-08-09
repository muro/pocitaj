package dev.aidistillery.pocitaj.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.FactMastery
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.SpeedBadge
import dev.aidistillery.pocitaj.logic.getSpeedBadge
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FactProgress(
    val factId: String,
    val mastery: FactMastery?,
    val speedBadge: SpeedBadge
)

data class LevelProgress(
    val progress: Float,
    val isMastered: Boolean
)

class ProgressReportViewModel(
    private val factMasteryDao: FactMasteryDao,
    private val activeUserId: Long
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
        factMasteryDao.getAllFactsForUser(activeUserId)
            .map { masteryList ->
                val masteryMap = masteryList.associateBy { it.factId }
                allFactsByOperation.mapValues { (operation, factIds) ->
                    factIds.map { factId ->
                        val mastery = masteryMap[factId]
                        val parts = factId.split('_')
                        val op1 = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val op2 = parts.getOrNull(2)?.toIntOrNull() ?: 0
                        val speedBadge =
                            getSpeedBadge(operation, op1, op2, mastery?.avgDurationMs ?: 0L)
                        FactProgress(factId, mastery, speedBadge)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Operation.entries.associateWith { emptyList() }
            )

    val levelProgressByOperation: StateFlow<Map<Operation, Map<String, LevelProgress>>> =
        factMasteryDao.getAllFactsForUser(activeUserId)
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
        val globals = App.app.globals
        return ProgressReportViewModel(
            factMasteryDao = globals.factMasteryDao,
            activeUserId = globals.activeUser.id
        ) as T
    }
}