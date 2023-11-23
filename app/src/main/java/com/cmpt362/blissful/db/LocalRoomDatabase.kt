package com.cmpt362.blissful.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.util.Converters

@Database(entities = [Post::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class LocalRoomDatabase : RoomDatabase() {
    abstract val postDatabaseDao: PostDatabaseDao
    abstract val userDatabaseDao: UserDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: LocalRoomDatabase? = null

        fun getInstance(context: Context) : LocalRoomDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        LocalRoomDatabase::class.java, "app_database").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
