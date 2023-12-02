package com.cmpt362.blissful.db.post

import com.google.firebase.firestore.Exclude
import java.util.*

data class Post(
    @get:Exclude var postId: String = "",
    var userId: String = "",
    var content: String = "",
    var postDateTime: Long = Calendar.getInstance().timeInMillis,
    var isPublic: Boolean = false,
    var likesCount: Int = 0,
    var imageUrl: String? = ""
) {
    constructor() : this("", "", "", 0, false, 0, "")

    // Convert Post object to Firebase
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "userId" to userId,
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

