package com.cmpt362.blissful.db.post

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class PostRepository(private val db: FirebaseFirestore) {

    val allPosts: Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPublicPosts(): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").whereEqualTo("isPublic", true).get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPostsByUserId(userId: String): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").whereEqualTo("userId", userId).get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPostsWithoutUserId(userId: String): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts").whereNotEqualTo("userId", userId).whereEqualTo("isPublic", true).get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    fun getPostById(postId: String): Flow<Post?> = flow {
        val docSnapshot = db.collection("posts").document(postId).get().await()
        emit(docSnapshot.toObject<Post>())
    }

    fun getPostsBetweenPostTime(startTime: Calendar, endTime: Calendar): Flow<List<Post>> = flow {
        val snapshot = db.collection("posts")
            .whereGreaterThanOrEqualTo("postDateTime", startTime.timeInMillis)
            .whereLessThanOrEqualTo("postDateTime", endTime.timeInMillis)
            .get().await()

        emit(snapshot.documents.mapNotNull { it.toObject<Post>() })
    }

    suspend fun insert(post: Post): String {
        val docRef = db.collection("posts").add(post.toMap()).await()
        return docRef.id // Firebase generates a unique ID for the post
    }

    suspend fun update(post: Post) {
        db.collection("posts").document(post.postId).set(post.toMap()).await()
    }

    suspend fun delete(postId: String) {
        db.collection("posts").document(postId).delete().await()
    }

    suspend fun deleteAll() {
        val snapshot = db.collection("posts").get().await()
        snapshot.documents.forEach { doc -> db.collection("posts").document(doc.id).delete() }
    }
}
