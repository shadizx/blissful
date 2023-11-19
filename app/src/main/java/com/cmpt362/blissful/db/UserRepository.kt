package com.cmpt362.blissful.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserRepository(private val userDatabaseDao: UserDatabaseDao) {

    val allUsers: Flow<List<User>> = userDatabaseDao.getAllUsers()

    suspend fun insert(user: User){
        CoroutineScope(IO).launch{
            userDatabaseDao.insertUser(user)
        }
    }

    suspend fun delete(id: Long){
        CoroutineScope(IO).launch {
            userDatabaseDao.deleteUser(id)
        }
    }

    suspend fun deleteAll(){
        CoroutineScope(IO).launch {
            userDatabaseDao.deleteAll()
        }
    }

    suspend fun isUserExist(username: String, password: String): Boolean {
        return userDatabaseDao.isUserExist(username, password)
    }
}