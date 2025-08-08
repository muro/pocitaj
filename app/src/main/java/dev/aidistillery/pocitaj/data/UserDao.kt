package dev.aidistillery.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun getUser(id: Long): User?
}
