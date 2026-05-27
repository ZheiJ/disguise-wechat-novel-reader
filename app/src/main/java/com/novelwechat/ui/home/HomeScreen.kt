package com.novelwechat.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelwechat.App
import com.novelwechat.R
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.ui.components.WeChatChatListItem
import com.novelwechat.ui.components.WeChatTitleBar
import com.novelwechat.ui.theme.WechatTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onBookClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val repo = remember {
        BookRepository(app.database.bookDao(), app.database.chapterDao(), context)
    }
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repo))
    val books by viewModel.books.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditCoverDialog by remember { mutableStateOf(false) }
    var bookIdForCover by remember { mutableLongStateOf(-1L) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch { viewModel.importBook(it) }
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { coverUri ->
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(coverUri)
                    val fileName = "cover_${bookIdForCover}.jpg"
                    val coversDir = java.io.File(context.filesDir, "covers")
                    if (!coversDir.exists()) coversDir.mkdirs()
                    val coverFile = java.io.File(coversDir, fileName)
                    inputStream?.use { input ->
                        java.io.FileOutputStream(coverFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("AvatarUpdate", "Saving cover for book ID: $bookIdForCover to ${coverFile.absolutePath}")
                    viewModel.updateBookCover(bookIdForCover, coverFile.absolutePath)
                    bookIdForCover = -1
                } catch (e: Exception) {
                    Log.e("AvatarUpdate", "Error saving cover", e)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(
            title = "微信(${books.sumOf { it.unreadCount }.takeIf { it > 0 } ?: 256})",
            showHomeActions = true,
            onAdd = { fileLauncher.launch(arrayOf("text/*", "application/epub+zip", "application/octet-stream")) },
        )

        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "点击右上角 + 导入小说",
                    color = WechatTheme.colors.textHint,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(books, key = { it.id }) { book ->
                    WeChatChatListItem(
                        title = book.title,
                        subtitle = buildBookSubtitle(book),
                        timeText = viewModel.formatTime(book.lastReadTime),
                        unreadCount = book.unreadCount,
                        isPinned = book.isPinned,
                        coverPath = book.coverPath,
                        fallbackAvatar = R.drawable.avatar_novel,
                        onClick = { onBookClick(book.id) },
                        onLongPress = {
                            selectedBook = book
                            showContextMenu = true
                        },
                    )
                }
            }
        }
    }

    if (showContextMenu && selectedBook != null) {
        AlertDialog(
            onDismissRequest = {
                showContextMenu = false
                selectedBook = null
            },
            title = { Text(text = selectedBook?.title ?: "") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showContextMenu = false
                            showEditNameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("修改名称")
                    }
                    TextButton(
                        onClick = {
                            showContextMenu = false
                            showEditCoverDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("修改头像")
                    }
                    TextButton(
                        onClick = {
                            showContextMenu = false
                            showDeleteConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("删除书籍")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showContextMenu = false
                    selectedBook = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && selectedBook != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("删除书籍") },
            text = { Text("确定要删除《${selectedBook?.title}》吗？删除后可在文件管理器重新导入。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedBook?.let { viewModel.deleteBook(it.id) }
                        showDeleteConfirmDialog = false
                        selectedBook = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showEditNameDialog && selectedBook != null) {
        var newTitle by remember { mutableStateOf(selectedBook?.title ?: "") }

        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("修改名称", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("书籍名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditNameDialog = false }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    selectedBook?.let { viewModel.updateBookTitle(it.id, newTitle) }
                                }
                                showEditNameDialog = false
                                selectedBook = null
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }

    if (showEditCoverDialog && selectedBook != null) {
        val bookId = selectedBook?.id ?: -1L
        AlertDialog(
            onDismissRequest = {
                showEditCoverDialog = false
                selectedBook = null
            },
            title = { Text("修改头像") },
            text = { Text("选择一张图片作为书籍封面") },
            confirmButton = {
                TextButton(onClick = {
                    bookIdForCover = bookId
                    showEditCoverDialog = false
                    selectedBook = null
                    coverPickerLauncher.launch(arrayOf("image/*"))
                }) {
                    Text("选择图片")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditCoverDialog = false
                    selectedBook = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun buildBookSubtitle(book: Book): String {
    val chapterPart = if (book.totalChapters > 0) "${book.totalChapters}章" else book.fileType.uppercase()
    return if (book.author.isBlank()) chapterPart else "$chapterPart · ${book.author}"
}
