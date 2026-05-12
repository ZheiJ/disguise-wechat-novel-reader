package com.novelwechat.data.repository

import android.util.Log
import android.content.Context
import android.net.Uri
import com.novelwechat.data.local.dao.BookDao
import com.novelwechat.data.local.dao.ChapterDao
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.local.entity.Chapter
import com.novelwechat.data.parser.EpubParser
import com.novelwechat.data.parser.ParseResult
import com.novelwechat.data.parser.TxtParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BookRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val context: Context,
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allBooksSorted: Flow<List<Book>> = bookDao.getAllBooksSorted()

    fun searchBooks(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    suspend fun getBookById(id: Long): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookById(id)
    }

    suspend fun importBook(uri: Uri): Long = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("无法打开文件")
        val fileName = getFileName(uri) ?: "未知.txt"

        // 复制文件到应用私有目录
        val ext = fileName.substringAfterLast(".", "txt").lowercase()
        val savedName = "${UUID.randomUUID()}.$ext"
        val booksDir = File(context.filesDir, "books")
        if (!booksDir.exists()) booksDir.mkdirs()
        val savedFile = File(booksDir, savedName)
        FileOutputStream(savedFile).use { out -> inputStream.copyTo(out) }
        inputStream.close()

        // 解析文件
        val result: ParseResult = when (ext) {
            "epub" -> EpubParser(context).parse(savedFile.inputStream(), fileName)
            else -> {
                val content = savedFile.readText(Charsets.UTF_8)
                TxtParser().parse(content, fileName)
            }
        }

        // 存入数据库
        val book = Book(
            title = result.title,
            author = result.author,
            filePath = savedFile.absolutePath,
            fileType = ext,
            totalChapters = result.chapters.size,
            addedTime = System.currentTimeMillis(),
            lastReadTime = System.currentTimeMillis(),
        )
        val bookId = bookDao.insert(book)

        // 存章节
        val chapters = result.chapters.map { ch ->
            Chapter(
                bookId = bookId,
                chapterIndex = ch.index,
                title = ch.title,
                content = ch.content,
                sentenceCount = 0,
            )
        }
        chapterDao.insertAll(chapters)

        bookId
    }

    suspend fun deleteBook(bookId: Long) = bookDao.softDelete(bookId)

    suspend fun updateLastReadTime(bookId: Long) {
        bookDao.updateLastReadTime(bookId, System.currentTimeMillis())
    }

    suspend fun togglePin(bookId: Long, pinned: Boolean) {
        bookDao.updatePinned(bookId, pinned)
    }

    suspend fun updateBookTitle(bookId: Long, title: String) {
        bookDao.updateTitle(bookId, title)
    }

    suspend fun updateBookCover(bookId: Long, coverPath: String) {
        Log.d("AvatarUpdate", "Repository updating cover for book ID: $bookId with path: $coverPath")
        bookDao.updateCoverPath(bookId, coverPath)
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) name = it.getString(nameIndex)
            }
        }
        return name ?: uri.lastPathSegment
    }
}
