package dev.aidistillery.pocitaj.ui.profile

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.aidistillery.pocitaj.App
import dev.aidistillery.pocitaj.data.ActiveUserManager
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserAppearance
import dev.aidistillery.pocitaj.data.UserDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class UserProfileViewModel(
    private val userDao: UserDao,
    private val exerciseAttemptDao: ExerciseAttemptDao,
    private val activeUserManager: ActiveUserManager
) : ViewModel() {

    open val users: StateFlow<List<User>> = userDao.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val activeUser: StateFlow<User> = activeUserManager.activeUserFlow

    fun addUser(name: String) {
        viewModelScope.launch {
            val currentUsers = userDao.getAllUsersFlow().first()
            val usedIcons = currentUsers.map { it.iconId }.toSet()
            val usedColors = currentUsers.map { it.color }.toSet()

            val availableIcons = UserAppearance.icons.keys.filter { it !in usedIcons }
            val nextIcon = if (availableIcons.isNotEmpty()) {
                availableIcons.random()
            } else {
                UserAppearance.icons.keys.random()
            }

            val allColors = UserAppearance.colors.map { it.toArgb() }
            val availableColors = allColors.filter { it !in usedColors }
            val nextColor = if (availableColors.isNotEmpty()) {
                availableColors.random()
            } else {
                allColors.random()
            }

            userDao.insert(
                User(
                    name = name,
                    iconId = nextIcon,
                    color = nextColor
                )
            )
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            if (activeUserManager.activeUser.id == user.id) {
                val defaultUser = userDao.getUser(1L)
                if (defaultUser != null) {
                    activeUserManager.setActiveUser(defaultUser)
                }
            }
            userDao.delete(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userDao.update(user)
        }
    }

    suspend fun getAttemptCountForUser(user: User): Int {
        return exerciseAttemptDao.getAttemptCountForUser(user.id)
    }

    fun setActiveUser(userId: Long) {
        viewModelScope.launch {
            val user = userDao.getUser(userId)
            if (user != null) {
                (App.app.globals.activeUserManager).setActiveUser(user)
            }
        }
    }
}

object UserProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val globals = App.app.globals
        return UserProfileViewModel(
            userDao = globals.userDao,
            exerciseAttemptDao = globals.exerciseAttemptDao,
            activeUserManager = globals.activeUserManager
        ) as T
    }
}
