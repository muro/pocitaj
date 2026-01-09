package dev.aidistillery.pocitaj.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Suppress("unused", "unused")
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    val activeUserId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[Keys.ACTIVE_USER_ID]
        }

    suspend fun setActiveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_USER_ID] = userId
        }
    }
}
