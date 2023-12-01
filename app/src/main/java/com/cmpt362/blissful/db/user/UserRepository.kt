package com.cmpt362.blissful.db.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    val allUsers: Flow<List<User>> = flow {
        val snapshot = db.collection("users").get().await()
        emit(snapshot.documents.mapNotNull { it.toObject<User>() })
    }

    suspend fun insert(user: User): String {
        val docRef = db.collection("users").add(user.toMap()).await()
        return docRef.id // Firebase generates a unique ID for the user
    }

    suspend fun delete(userId: String) {
        db.collection("users").document(userId).delete().await()
    }

    suspend fun isUserExist(username: String, password: String): Boolean {
        val snapshot = db.collection("users").whereEqualTo("username", username).whereEqualTo("password", password).get().await()
        return !snapshot.isEmpty
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        val snapshot = db.collection("users").whereEqualTo("username", username).get().await()
        return !snapshot.isEmpty
    }

    suspend fun getIdForUser(username: String): String? {
        val snapshot = db.collection("users").whereEqualTo("username", username).limit(1).get().await()
        return if (!snapshot.isEmpty) snapshot.documents[0].id else null
    }

    suspend fun getUsernameForUserId(userId: String): String? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject<User>()?.username
    }
}
