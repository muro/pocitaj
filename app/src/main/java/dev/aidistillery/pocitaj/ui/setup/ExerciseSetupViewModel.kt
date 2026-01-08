package dev.aidistillery.pocitaj.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ActiveUserManager
import dev.aidistillery.pocitaj.data.FactMasteryDao
import dev.aidistillery.pocitaj.data.Operation
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.logic.Curriculum
import dev.aidistillery.pocitaj.logic.Level
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LevelStatus(
    val level: Level,
    val isUnlocked: Boolean,
    val progress: Float
)

data class OperationLevels(
    val operation: Operation,
    val levelStatuses: List<LevelStatus>
)

class ExerciseSetupViewModel(
    private val factMasteryDao: FactMasteryDao,
    private val activeUserManager: ActiveUserManager
) : ViewModel() {

    val activeUser: StateFlow<User> = activeUserManager.activeUserFlow

    companion object {
        private const val MASTERY_STRENGTH = 5
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val operationLevels: StateFlow<List<OperationLevels>> =
        activeUserManager.activeUserFlow.flatMapLatest { activeUser ->
            factMasteryDao.getAllFactsForUser(activeUser.id)
                .map { masteryList ->
                    val masteryMap = masteryList.associateBy { it.factId }
                    val allLevels = Curriculum.getAllLevels()

                    // Determine mastered levels for unlocking prerequisites
                    val masteredLevelIds = allLevels.filter { level ->
                        val factsInLevel = level.getAllPossibleFactIds()
                        if (factsInLevel.isEmpty()) return@filter true
                        val masteredFactCount = factsInLevel.count { factId ->
                            (masteryMap[factId]?.strength ?: 0) >= MASTERY_STRENGTH
                        }
                        masteredFactCount == factsInLevel.size
                    }.map { it.id }.toSet()

                    allLevels.groupBy { it.operation }.map { (op, levels) ->
                        val levelStates = levels.map { level ->
                            val factsInLevel = level.getAllPossibleFactIds()
                            val progress = if (factsInLevel.isEmpty()) {
                                0f
                            } else {
                                val totalStrength = factsInLevel.sumOf { factId ->
                                    masteryMap[factId]?.strength ?: 0
                                }
                                val maxPossibleStrength = factsInLevel.size * MASTERY_STRENGTH
                                totalStrength.toFloat() / maxPossibleStrength
                            }
                            val isUnlocked = level.prerequisites.all { it in masteredLevelIds }
                            LevelStatus(level, isUnlocked, progress)
                        }
                        OperationLevels(op, levelStates)
                    }
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

object ExerciseSetupViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val globals = App.app.globals
        return ExerciseSetupViewModel(
            factMasteryDao = globals.factMasteryDao,
            activeUserManager = globals.activeUserManager
        ) as T
    }
}
