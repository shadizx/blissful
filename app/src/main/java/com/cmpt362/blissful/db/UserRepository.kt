package com.cmpt362.blissful.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserRepository(private val userDatabaseDao: UserDatabaseDao) {

    val allUsers: Flow<List<User>> = userDatabaseDao.getAllUsers()

    fun insert(user: User){
        CoroutineScope(IO).launch{
            userDatabaseDao.insertUser(user)
        }
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            userDatabaseDao.deleteUser(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            userDatabaseDao.deleteAll()
        }
    }
}