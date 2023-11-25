package com.cmpt362.blissful.db.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.cmpt362.blissful.db.user.User
import java.util.*

@Entity(
    tableName = "post_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    val postId: Int = 0,

    @ColumnInfo(name = "userId")
    val userId: Int,

    /**
     * In Room, when we use a String type for a column, it corresponds to the TEXT data type in SQLite (which is Room's DB engine).
     * SQLite's TEXT data type does not require a size limit to be defined like VARCHAR in MySQL.
     * However, when implementing the logic of posts content, it is better to set a word limit (500 words?... etc).
     * This suggestion is to ensure the storage and displaying of the post content data can be done properly.
     */
    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "postDateTime")
    val postDateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "isPublic")
    val isPublic: Boolean = false,

    @ColumnInfo(name = "likesCount")
    val likesCount: Int = 0,

    @ColumnInfo(name = "commentsCount")
    val commentsCount: Int = 0,

    @ColumnInfo(name = "lastUpdateDateTime")
    val lastUpdateDateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "location")
    val location: String?
)