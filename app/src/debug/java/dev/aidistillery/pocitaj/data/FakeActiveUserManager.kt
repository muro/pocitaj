package dev.aidistillery.pocitaj.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class FakeActiveUserManager(initialUser: User? = null) : ActiveUserManager {
    private val defaultActiveUser = User(id = 1, name = "Default User")
    private val _activeUserFlow = MutableStateFlow(initialUser ?: defaultActiveUser)
    override val activeUserFlow: StateFlow<User> = _activeUserFlow.asStateFlow()

    fun reset() {
        runBlocking {
            setActiveUser(defaultActiveUser)
        }
    }


    override var activeUser: User
        get() = _activeUserFlow.value
        set(user) {
            _activeUserFlow.value = user
        }

    override suspend fun init() {
        // Not needed for fakes
    }

    override suspend fun setActiveUser(user: User) {
        _activeUserFlow.value = user
    }
}
