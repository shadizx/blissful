package com.cmpt362.blissful.db.user

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId var id: String = "",
    var username: String = "",
    var password: String = "",
    var profileImgUrl: String = "",
) {
    constructor() : this("", "", "")

    // Convert User object to Firebase
    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "password" to password,
            "profileImgUrl" to profileImgUrl
        )
    }
}
