package com.cmpt362.blissful.db.user

import com.google.firebase.firestore.Exclude

data class User(
    @get:Exclude var userId: String = "",

    var username: String = "",

    var password: String = ""
) {
    constructor() : this("", "", "")

    // Convert User object to Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "password" to password
        )
    }
}
