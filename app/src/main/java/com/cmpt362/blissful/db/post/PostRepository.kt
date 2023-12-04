package com.cmpt362.blissful.db.post

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PostRepository(private val db: FirebaseFirestore) {

    val allPosts: Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPublicPosts(): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").orderBy("isPublic").whereEqualTo("isPublic", true)
            .orderBy("postDateTime", Direction.DESCENDING).get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPostsByUserId(userId: String): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").orderBy("userId").whereEqualTo("userId", userId)
            .orderBy("postDateTime", Direction.DESCENDING).get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPostsWithoutUserId(userId: String): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").orderBy("isPublic").whereEqualTo("isPublic", true)
            .orderBy("postDateTime", Direction.DESCENDING)
            .get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() }.filter { it.userId != userId })
    }

    suspend fun insert(post: Post): String {
        val docRef = db.collection("posts").add(post.toMap()).await()
        return docRef.id // Firebase generates a unique ID for the post
    }

    suspend fun likePost(postId: String) {
        db.collection("posts").document(postId).update("likesCount", FieldValue.increment(1))
            .await()
    }

    suspend fun unlikePost(postId: String) {
        db.collection("posts").document(postId).update("likesCount", FieldValue.increment(-1))
            .await()
    }
}
