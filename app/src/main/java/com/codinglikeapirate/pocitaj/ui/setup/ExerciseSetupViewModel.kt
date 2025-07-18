package com.codinglikeapirate.pocitaj.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.codinglikeapirate.pocitaj.App
import com.codinglikeapirate.pocitaj.data.FactMasteryDao
import com.codinglikeapirate.pocitaj.data.Operation
import com.codinglikeapirate.pocitaj.logic.Curriculum
import com.codinglikeapirate.pocitaj.logic.Level
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LevelStatus(
    val level: Level,
    val isUnlocked: Boolean,
    val isMastered: Boolean
)

data class OperationLevels(
    val operation: Operation,
    val levels: List<LevelStatus>
)

class ExerciseSetupViewModel(
    factMasteryDao: FactMasteryDao
) : ViewModel() {

    companion object {
        private const val MASTERY_STRENGTH = 5
    }

    val operationLevels: StateFlow<List<OperationLevels>> =
        factMasteryDao.getAllFactsForUser(1) // Assuming user ID 1
            .map { masteryList ->
                val masteredFacts = masteryList.filter { it.strength >= MASTERY_STRENGTH }.map { it.factId }.toSet()
                val allLevels = Curriculum.getAllLevels()
                val masteredLevelIds = allLevels.filter { level ->
                    level.getAllPossibleFactIds().all { it in masteredFacts }
                }.map { it.id }.toSet()

                allLevels.groupBy { it.operation }.map { (op, levels) ->
                    val levelStates = levels.map { level ->
                        val isMastered = level.id in masteredLevelIds
                        val isUnlocked = level.prerequisites.all { it in masteredLevelIds }
                        LevelStatus(level, isUnlocked, isMastered)
                    }
                    OperationLevels(op, levelStates)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

object ExerciseSetupViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App
        return ExerciseSetupViewModel(
            factMasteryDao = application.database.factMasteryDao()
        ) as T
    }
}
