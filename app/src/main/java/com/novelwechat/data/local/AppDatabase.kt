package com.novelwechat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.novelwechat.data.local.dao.BookDao
import com.novelwechat.data.local.dao.ChapterDao
import com.novelwechat.data.local.dao.ReadProgressDao
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.local.entity.Chapter
import com.novelwechat.data.local.entity.ReadProgress

@Database(
    entities = [Book::class, Chapter::class, ReadProgress::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readProgressDao(): ReadProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "novel_wechat.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
