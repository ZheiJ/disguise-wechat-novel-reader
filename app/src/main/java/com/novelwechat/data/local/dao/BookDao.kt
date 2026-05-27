package com.novelwechat.data.local.dao

import androidx.room.*
import com.novelwechat.data.local.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE isDeleted = 0 ORDER BY isPinned DESC, lastReadTime DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isDeleted = 0 ORDER BY title ASC")
    fun getAllBooksSorted(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Query("SELECT COUNT(*) FROM books WHERE filePath = :filePath AND isDeleted = 0")
    suspend fun countActiveByFilePath(filePath: String): Int

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' AND isDeleted = 0")
    fun searchBooks(query: String): Flow<List<Book>>

    @Insert
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Query("UPDATE books SET lastReadTime = :time WHERE id = :bookId")
    suspend fun updateLastReadTime(bookId: Long, time: Long)

    @Query("UPDATE books SET isPinned = :pinned WHERE id = :bookId")
    suspend fun updatePinned(bookId: Long, pinned: Boolean)

    @Query("UPDATE books SET title = :title WHERE id = :bookId")
    suspend fun updateTitle(bookId: Long, title: String)

    @Query("UPDATE books SET coverPath = :coverPath WHERE id = :bookId")
    suspend fun updateCoverPath(bookId: Long, coverPath: String)

    @Query("UPDATE books SET isDeleted = 1 WHERE id = :bookId")
    suspend fun softDelete(bookId: Long)
}
