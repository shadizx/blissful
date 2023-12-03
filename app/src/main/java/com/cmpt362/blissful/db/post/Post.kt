package com.cmpt362.blissful.db.post

import com.google.firebase.firestore.DocumentId
import java.util.Calendar

data class Post(
    @DocumentId var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var content: String = "",
    var postDateTime: Long = Calendar.getInstance().timeInMillis,
    var isPublic: Boolean = false,
    var likesCount: Int = 0,
    var imageUrl: String? = ""
) {
    constructor() : this("", "", "", "", 0, false, 0, "")

    // Convert Post object to Firebase
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "userId" to userId,
            "userName" to userName,
            "content" to content,
            "postDateTime" to postDateTime,
            "isPublic" to isPublic,
            "likesCount" to likesCount
        )

        // Only include imageUrl in the map if it's not null
        imageUrl?.let { map["imageUrl"] = it }

        return map
    }
}

