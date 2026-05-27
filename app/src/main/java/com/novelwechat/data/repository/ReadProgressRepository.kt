package com.novelwechat.data.repository

import com.novelwechat.data.local.dao.ChapterDao
import com.novelwechat.data.local.dao.ChapterTitle
import com.novelwechat.data.local.dao.ReadProgressDao
import com.novelwechat.data.local.entity.Chapter
import com.novelwechat.data.local.entity.ReadProgress
import kotlinx.coroutines.flow.Flow

class ReadProgressRepository(
    private val readProgressDao: ReadProgressDao,
    private val chapterDao: ChapterDao,
) {
    suspend fun getProgress(bookId: Long): ReadProgress? = readProgressDao.getProgress(bookId)

    suspend fun saveProgress(progress: ReadProgress) = readProgressDao.saveProgress(progress)

    suspend fun getChapter(bookId: Long, chapterIndex: Int): Chapter? =
        chapterDao.getChapter(bookId, chapterIndex)

    suspend fun getChapterTitles(bookId: Long): List<ChapterTitle> =
        chapterDao.getChapterTitles(bookId)

    fun getChapters(bookId: Long): Flow<List<Chapter>> =
        chapterDao.getChaptersByBookId(bookId)

    suspend fun getChapterCount(bookId: Long): Int = chapterDao.getChapterCount(bookId)
}
