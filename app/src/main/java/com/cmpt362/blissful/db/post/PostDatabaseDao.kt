package com.cmpt362.blissful.db.post

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Calendar


@Dao
interface PostDatabaseDao {

    @Insert
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM post_table WHERE isPublic = 1")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM post_table WHERE userId = :userId")
    fun getAllPostsByUserId(userId: Int): Flow<List<Post>>

    @Query("SELECT * FROM post_table WHERE postId = :postId")
    fun getPostById(postId: Int): Flow<Post>

    @Query("SELECT * FROM post_table WHERE postDateTime >= :startTime AND postDateTime <= :endTime ORDER BY postDateTime DESC")
    fun getPostsBetweenPostTime(startTime: Calendar, endTime: Calendar): Flow<List<Post>>

    @Update
    suspend fun updatePost(post: Post)

    @Query("DELETE FROM post_table WHERE postId = :postId")
    suspend fun deletePost(postId: Int)

    @Query("DELETE FROM post_table")
    suspend fun deleteAll()


}