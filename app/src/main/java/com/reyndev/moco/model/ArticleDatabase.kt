package com.reyndev.moco.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database([Article::class], version = 1, exportSchema =  false)
abstract class ArticleDatabase: RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        fun getDatabase(context: Context): ArticleDatabase =
            INSTANCE ?: synchronized(this) {
                return INSTANCE.let {
                    Room.databaseBuilder(
                        context,
                        ArticleDatabase::class.java,
                        "article_database")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
    }
}