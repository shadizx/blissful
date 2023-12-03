package com.cmpt362.blissful.db.like

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class LikeRepository(private val db: FirebaseFirestore) {
    suspend fun insert(like: Like): String {
        val docRef = db.collection("likes").add(like.toMap()).await()
        return docRef.id // Firebase generates a unique ID for the like
    }

    suspend fun delete(likeId: String) {
        db.collection("likes").document(likeId).delete().await()
    }

    suspend fun delete(userId: String, postId: String) {
        val snapshot =
            db.collection("likes").whereEqualTo("userId", userId).whereEqualTo("postId", postId)
                .get().await()
        snapshot.documents.forEach { it.reference.delete() }
    }

    suspend fun getPostsThatUserLiked(userId: String): Set<String> {
        val snapshot = db.collection("likes").whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject<Like>()?.postId }.toSet()
    }
}