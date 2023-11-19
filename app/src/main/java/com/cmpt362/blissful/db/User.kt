package com.cmpt362.blissful.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @PrimaryKey(autoGenerate = true)
    var username: String = "",

    @ColumnInfo(name = "password_column")
    var password: String = "",

)