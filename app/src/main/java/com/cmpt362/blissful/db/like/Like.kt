package com.cmpt362.blissful.db.like

import com.google.firebase.firestore.Exclude

data class Like(
    @get:Exclude var likeId: String = "",
    var userId: String = "",
    var postId: String = ""
) {
    constructor() : this("", "", "")

    // Convert Like object to Firebase
    fun toMap(): Map<String, Any> {
        return mutableMapOf(
            "userId" to userId,
            "postId" to postId
        )
    }
}
