package com.codinglikeapirate.pocitaj.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long
}
