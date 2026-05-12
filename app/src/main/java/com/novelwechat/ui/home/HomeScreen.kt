package com.novelwechat.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelwechat.App
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.ui.components.WeChatChatListItem
import com.novelwechat.ui.components.WeChatSearchBar
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

    // 长按菜单状态
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    // 对话框状态
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditCoverDialog by remember { mutableStateOf(false) }

    // 用于封面选择时保存书籍ID（因为selectedBook会在对话框关闭后被清空）
    var bookIdForCover by remember { mutableLongStateOf(-1L) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                viewModel.importBook(it)
            }
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
                        bookIdForCover = -1 // 重置
                    } catch (e: Exception) {
                        Log.e("AvatarUpdate", "Error saving cover", e)
                        e.printStackTrace()
                    }
                }
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(
            title = "微信",
            onMore = {
                fileLauncher.launch(arrayOf("text/plain", "application/epub+zip"))
            },
        )
        WeChatSearchBar(placeholder = "搜索")

        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "点击右上角 ⋯ 导入小说",
                    color = WechatTheme.colors.textHint,
                    fontSize = 14.sp,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(books, key = { it.id }) { book ->
                    WeChatChatListItem(
                        title = book.title,
                        subtitle = "第${book.totalChapters}章 · ${book.author}",
                        timeText = viewModel.formatTime(book.lastReadTime),
                        unreadCount = book.unreadCount,
                        isPinned = book.isPinned,
                        coverPath = book.coverPath,
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

    // 长按弹出菜单
    if (showContextMenu && selectedBook != null) {
        AlertDialog(
            onDismissRequest = {
                showContextMenu = false
                selectedBook = null
            },
            title = {
                Text(text = selectedBook?.title ?: "")
            },
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

    // 删除确认对话框
    if (showDeleteConfirmDialog && selectedBook != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
            },
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
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                }) {
                    Text("取消")
                }
            }
        )
    }

    // 修改名称对话框
    if (showEditNameDialog && selectedBook != null) {
        var newTitle by remember { mutableStateOf(selectedBook?.title ?: "") }

        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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

    // 修改头像提示对话框
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
                    bookIdForCover = bookId // 保存书籍ID
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