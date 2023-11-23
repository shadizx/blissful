package com.cmpt362.blissful.db.user
import kotlinx.coroutines.flow.Flow

/**
 * Splitting repositories and DAOs for each table (e.g., User and Post) enhances the maintainability and clarity of the code.
 * Each repository and DAO focuses on each table.
 * This separation allows for more organized and manageable code, and also increases the scalability of the project.
 * This approach is also considered a common best practices especially for large Android Project.
 */

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

    suspend fun isUsernameTaken(username: String): Boolean {
        return userDatabaseDao.isUsernameTaken(username)
    }
}