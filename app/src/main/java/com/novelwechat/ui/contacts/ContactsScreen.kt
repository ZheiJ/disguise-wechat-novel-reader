package com.novelwechat.ui.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelwechat.App
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.ui.components.WeChatContactItem
import com.novelwechat.ui.components.WeChatSearchBar
import com.novelwechat.ui.components.WeChatSectionHeader
import com.novelwechat.ui.components.WeChatTitleBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsViewModel(private val repo: BookRepository) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    init {
        GlobalScope.launch(Dispatchers.Main) {
            repo.allBooksSorted.collect { list ->
                _books.value = list
            }
        }
    }
}

class ContactsViewModelFactory(private val repo: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ContactsViewModel(repo) as T
    }
}

@Composable
fun ContactsScreen(
    onBookClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val repo = remember {
        BookRepository(app.database.bookDao(), app.database.chapterDao(), context)
    }
    val viewModel: ContactsViewModel = viewModel(factory = ContactsViewModelFactory(repo))
    val books by viewModel.books.collectAsState()

    val grouped = books.groupBy { getPinyinInitial(it.title) }.toSortedMap()

    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(title = "通讯录")
        WeChatSearchBar(placeholder = "搜索")

        LazyColumn(modifier = Modifier.weight(1f)) {
            grouped.forEach { (letter, bookList) ->
                item(key = "header_$letter") {
                    WeChatSectionHeader(letter = letter)
                }
                items(bookList, key = { it.id }) { book ->
                    WeChatContactItem(
                        name = book.title,
                        onClick = { onBookClick(book.id) },
                    )
                }
            }
        }
    }
}

private fun getPinyinInitial(text: String): String {
    if (text.isEmpty()) return "#"
    val first = text[0].uppercaseChar()
    return if (first in 'A'..'Z') first.toString() else "#"
}
