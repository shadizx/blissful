package com.cmpt362.blissful.db.user

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDatabaseDao: UserDatabaseDao) {

    val allUsers: Flow<List<User>> = userDatabaseDao.getAllUsers()

    suspend fun insert(user: User) {
        userDatabaseDao.insertUser(user)
    }

    suspend fun delete(userId: Int) {
        userDatabaseDao.deleteUser(userId)
    }

    suspend fun deleteAll() {
        userDatabaseDao.deleteAll()
    }

    suspend fun isUserExist(username: String, password: String): Boolean {
        return userDatabaseDao.isUserExist(username, password)
    }
}