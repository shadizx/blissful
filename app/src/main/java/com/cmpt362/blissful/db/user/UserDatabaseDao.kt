package com.cmpt362.blissful.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDatabaseDao {

    @Insert
    suspend fun insertUser(user: User)
    @Query("SELECT * FROM user_table")
    fun getAllUsers(): Flow<List<User>>

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("DELETE FROM user_table WHERE userId = :key")
    suspend fun deleteUser(key: Int)

    @Query("SELECT EXISTS(SELECT * FROM user_table WHERE username = :username AND password = :password)")
    suspend fun isUserExist(username: String, password: String): Boolean

}