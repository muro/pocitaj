package dev.aidistillery.pocitaj.data

import androidx.compose.ui.graphics.toArgb
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM user")
    fun getAllUsersFlow(): Flow<List<User>>

    fun getAllUsers(): Flow<List<User>> = getAllUsersFlow().map { users ->
        users.map { user ->
            if (user.id == 1L) {
                user.copy(iconId = "robot", color = UserAppearance.colors.last().toArgb())
            } else {
                user
            }
        }
    }

    @Query("SELECT * FROM user WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun getUserFlow(id: Long): User?

    suspend fun getUser(id: Long): User? = getUserFlow(id)?.let { user ->
        if (user.id == 1L) {
            user.copy(iconId = "robot", color = UserAppearance.colors.last().toArgb())
        } else {
            user
        }
    }

    @Delete
    suspend fun delete(user: User)
}
