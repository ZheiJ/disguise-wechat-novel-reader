package com.novelwechat.data.local.dao

import androidx.room.*
import com.novelwechat.data.local.entity.ReadProgress

@Dao
interface ReadProgressDao {
    @Query("SELECT * FROM read_progress WHERE bookId = :bookId LIMIT 1")
    suspend fun getProgress(bookId: Long): ReadProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ReadProgress)

    @Query("DELETE FROM read_progress WHERE bookId = :bookId")
    suspend fun deleteProgress(bookId: Long)
}
