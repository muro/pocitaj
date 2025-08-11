package dev.aidistillery.pocitaj.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface ActiveUserManager {
    val activeUser: User

    suspend fun init()
    suspend fun setActiveUser(user: User)
}

class DataStoreActiveUserManager(
    private val context: Context,
    private val userDao: UserDao
) : ActiveUserManager {

    override lateinit var activeUser: User
        private set

    private object Keys {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    override suspend fun init() {
        // First, ensure the default user exists.                                                                                                   │
        if (userDao.getUser(1L) == null) {
            userDao.insert(User(id = 1, name = "Default User"))
        }

        val activeUserId = context.dataStore.data
            .map { preferences ->
                preferences[Keys.ACTIVE_USER_ID]
            }.first()

        val user: User? = userDao.getUser(activeUserId ?: 1L) ?: userDao.getUser(1L)
        if (user == null) {
            throw IllegalStateException("Default user not found and could not be created.")
        }
        activeUser = user
    }

    override suspend fun setActiveUser(user: User) {
        activeUser = user
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_USER_ID] = user.id
        }
        init()
    }
}
