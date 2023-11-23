package com.cmpt362.blissful.db.post
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PostRepository(private val postDatabaseDao: PostDatabaseDao) {

    fun getAllPosts(): Flow<List<Post>> = postDatabaseDao.getAllPosts()

    fun getPostsByUserId(userId: Int): Flow<List<Post>> =
        postDatabaseDao.getAllPostsByUserId(userId)

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
