package com.cmpt362.blissful.db.like

import com.google.firebase.firestore.DocumentId

data class Like(
    @DocumentId var likeId: String = "",
    var userId: String = "",
    var postId: String = ""
) {
    // Convert Like object to Firebase
    fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "userId" to userId,
            "postId" to postId
        )
    }
}
