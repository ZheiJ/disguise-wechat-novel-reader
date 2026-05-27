package com.novelwechat.data.local.dao

import androidx.room.*
import com.novelwechat.data.local.entity.Chapter
import kotlinx.coroutines.flow.Flow

data class ChapterTitle(
    val chapterIndex: Int,
    val title: String,
)

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun getChaptersByBookId(bookId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND chapterIndex = :index LIMIT 1")
    suspend fun getChapter(bookId: Long, index: Int): Chapter?

    @Query("SELECT chapterIndex, title FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    suspend fun getChapterTitles(bookId: Long): List<ChapterTitle>

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId")
    suspend fun getChapterCount(bookId: Long): Int

    @Insert
    suspend fun insertAll(chapters: List<Chapter>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: Long)
}
