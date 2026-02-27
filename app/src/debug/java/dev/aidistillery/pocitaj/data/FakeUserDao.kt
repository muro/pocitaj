package dev.aidistillery.pocitaj.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeUserDao : UserDao {
    private val _usersFlow = MutableStateFlow<List<User>>(emptyList())
    override fun getAllUsersFlow(): StateFlow<List<User>> = _usersFlow.asStateFlow()
    override fun getAllUsers(): StateFlow<List<User>> = getAllUsersFlow()

    override suspend fun insert(user: User): Long {
        val newId = (_usersFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newUser = user.copy(id = newId)
        _usersFlow.update { it + newUser }
        return newId
    }

    override suspend fun update(user: User) {
        _usersFlow.update { list ->
            list.map { if (it.id == user.id) user else it }
        }
    }

    override suspend fun getUserByName(name: String): User? {
        return _usersFlow.value.find { it.name == name }
    }

    override suspend fun getUserFlow(id: Long): User? {
        return _usersFlow.value.find { it.id == id }
    }

    override suspend fun getUser(id: Long): User? = getUserFlow(id)

    override suspend fun delete(user: User) {
        _usersFlow.update { list ->
            list.filter { it.id != user.id }
        }
    }
}
