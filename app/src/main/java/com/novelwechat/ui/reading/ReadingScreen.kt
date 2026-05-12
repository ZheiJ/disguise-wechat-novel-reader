package com.novelwechat.ui.reading

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelwechat.App
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.data.repository.ReadProgressRepository
import com.novelwechat.ui.components.*
import com.novelwechat.ui.theme.WechatTheme
import kotlinx.coroutines.launch

private const val TAG = "ReadingScreen"

@Composable
fun ReadingScreen(
    bookId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val repo = remember {
        BookRepository(app.database.bookDao(), app.database.chapterDao(), context)
    }
    val progressRepo = remember {
        ReadProgressRepository(app.database.readProgressDao(), app.database.chapterDao())
    }
    val viewModel: ReadingViewModel = viewModel(factory = ReadingViewModelFactory(repo, progressRepo))
    val state by viewModel.state.collectAsState()
    val colors = WechatTheme.colors
    val listState = rememberLazyListState()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    // 记录上一次的章节索引，章节切换时恢复滚动位置
    var previousChapterIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.chapterIndex, state.isLoading) {
        if (!state.isLoading && state.sentences.isNotEmpty()) {
            if (state.chapterIndex != previousChapterIndex) {
                // 章节切换了
                val targetIndex = if (previousChapterIndex == -1) {
                    // 首次加载，恢复到上次保存的位置
                    state.savedSentenceIndex.coerceIn(0, state.sentences.lastIndex)
                } else {
                    // 切换到其他章节（上/下翻），回到开头
                    0
                }
                Log.d(TAG, "scrollTo: chapterIndex=${state.chapterIndex}, " +
                        "previousChapterIndex=$previousChapterIndex, " +
                        "targetIndex=$targetIndex, " +
                        "savedSentenceIndex=${state.savedSentenceIndex}")
                listState.scrollToItem(targetIndex)
                previousChapterIndex = state.chapterIndex
            }
        }
    }

    // 监听滚动位置变化来保存进度
    var lastSavedIndex by remember { mutableIntStateOf(-1) }
    var lastSavedChapter by remember { mutableIntStateOf(-1) }

    LaunchedEffect(listState.firstVisibleItemIndex, state.chapterIndex) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentChapter = state.chapterIndex

        if (currentIndex >= 0 && state.sentences.isNotEmpty()
            && (currentIndex != lastSavedIndex || currentChapter != lastSavedChapter)
        ) {
            Log.d(TAG, "scroll: firstVisibleIndex=$currentIndex, chapterIndex=$currentChapter")
            viewModel.saveProgress(
                sentenceIndex = currentIndex,
                chapterIndex = currentChapter,
            )
            lastSavedIndex = currentIndex
            lastSavedChapter = currentChapter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(
            title = state.chapterTitle.ifEmpty { state.bookTitle },
            onBack = { onBack() },
            onMore = { },
            avatarPath = state.coverPath,
            subtitle = state.author,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(colors.chatBackground),
        ) {
            if (state.isLoading) {
                Text(
                    text = "加载中...",
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    if (state.chapterTitle.isNotEmpty()) {
                        item(key = "chapter_title") {
                            ChatTimeLabel(text = state.chapterTitle)
                        }
                    }

                    itemsIndexed(
                        state.sentences,
                        key = { index, _ -> "sentence_${state.chapterIndex}_$index" }
                    ) { index, sentence ->
                        if (index > 0 && index % 8 == 0) {
                            ParagraphTimeLabel(paragraphIndex = index / 8)
                        }
                        ChatBubble(
                            text = sentence.text,
                            isFromMe = sentence.isDialogue,
                            fontSize = state.fontSize,
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        WeChatChatInputBar(
            chapterIndex = state.chapterIndex,
            totalChapters = state.totalChapters,
            isFirstChapter = state.isFirstChapter,
            isLastChapter = state.isLastChapter,
            isLoading = state.isLoading,
            onPrevChapter = { viewModel.prevChapter() },
            onNextChapter = { viewModel.nextChapter() },
        )
    }
}

@Composable
private fun WeChatChatInputBar(
    chapterIndex: Int,
    totalChapters: Int,
    isFirstChapter: Boolean,
    isLastChapter: Boolean,
    isLoading: Boolean,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
) {
    val colors = WechatTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(color = colors.divider, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.chatInputBg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🎤",
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable(enabled = !isFirstChapter && !isLoading) { onPrevChapter() },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(colors.background, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "第${chapterIndex + 1}章",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "➕",
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable(enabled = !isLastChapter && !isLoading) { onNextChapter() },
            )
        }
    }
}