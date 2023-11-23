package com.cmpt362.blissful.db.post

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.util.Converters
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

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "postDateTime")
    val initialPostDateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "title")
    val title: String?,

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