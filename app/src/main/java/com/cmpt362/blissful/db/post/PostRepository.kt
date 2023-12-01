package com.cmpt362.blissful.db.post
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Splitting repositories and DAOs for each table (e.g., User and Post) enhances the maintainability and clarity of the code.
 * Each repository and DAO focuses on each table.
 * This separation allows for more organized and manageable code, and also increases the scalability of the project.
 * This approach is also considered a common best practices especially for large Android Project.
 */

class PostRepository(private val postDatabaseDao: PostDatabaseDao) {

    fun getAllPosts(): Flow<List<Post>> = postDatabaseDao.getAllPosts()

    fun getPostsByUserId(userId: String): Flow<List<Post>> =
        postDatabaseDao.getAllPostsByUserId(userId)

    fun getPostsWithoutUserId(userId: String): Flow<List<Post>> =
        postDatabaseDao.getAllPostsWithoutUserId(userId)

    fun getPostById(postId: Int): Flow<Post> = postDatabaseDao.getPostById(postId)

    fun getPostsBetweenPostTime(startTime: Calendar, endTime: Calendar): Flow<List<Post>> =
        postDatabaseDao.getPostsBetweenPostTime(startTime, endTime)

    suspend fun insert(post: Post) {
        postDatabaseDao.insertPost(post)
    }

    suspend fun update(post: Post) {
        postDatabaseDao.updatePost(post)
    }

    suspend fun delete(id: Int) {
        postDatabaseDao.deletePost(id)
    }

    suspend fun deleteAll() {
        postDatabaseDao.deleteAll()
    }
}
