package com.novelwechat.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val repo: BookRepository) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    init {
        GlobalScope.launch(Dispatchers.Main) {
            repo.allBooks.collect { list ->
                _books.value = list
            }
        }
    }

    suspend fun importBook(uri: Uri): Long {
        _importState.value = ImportState.Importing
        return try {
            val id = repo.importBook(uri)
            _importState.value = ImportState.Success
            id
        } catch (e: Exception) {
            _importState.value = ImportState.Error(e.message ?: "导入失败")
            -1L
        }
    }

    fun deleteBook(bookId: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            repo.deleteBook(bookId)
        }
    }

    fun updateBookTitle(bookId: Long, title: String) {
        GlobalScope.launch(Dispatchers.IO) {
            repo.updateBookTitle(bookId, title)
        }
    }

    fun updateBookCover(bookId: Long, coverPath: String) {
        Log.d("AvatarUpdate", "ViewModel updating cover for book ID: $bookId")
        GlobalScope.launch(Dispatchers.IO) {
            repo.updateBookCover(bookId, coverPath)
        }
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val now = Calendar.getInstance()
        val msg = Calendar.getInstance().apply { timeInMillis = timestamp }
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        return when {
            now.get(Calendar.YEAR) == msg.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == msg.get(Calendar.DAY_OF_YEAR) -> fmt.format(msg.time)

            now.get(Calendar.DAY_OF_YEAR) - msg.get(Calendar.DAY_OF_YEAR) == 1 -> "昨天"

            else -> SimpleDateFormat("M月d日", Locale.getDefault()).format(msg.time)
        }
    }

    sealed class ImportState {
        object Idle : ImportState()
        object Importing : ImportState()
        object Success : ImportState()
        data class Error(val message: String) : ImportState()
    }
}

class HomeViewModelFactory(private val repo: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repo) as T
    }
}
