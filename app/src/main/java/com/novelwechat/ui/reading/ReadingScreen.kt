package com.novelwechat.ui.reading

import android.content.Intent
import android.content.ComponentName
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novelwechat.App
import com.novelwechat.R
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.data.repository.ReadProgressRepository
import com.novelwechat.ui.components.ChatBubble
import com.novelwechat.ui.components.ChatTimeLabel
import com.novelwechat.ui.components.ChapterDrawer
import com.novelwechat.ui.components.ParagraphTimeLabel
import com.novelwechat.ui.components.WeChatTitleBar
import com.novelwechat.ui.theme.WechatTheme
import com.novelwechat.util.UserProfileManager

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
    val myName = remember { UserProfileManager.getNickname(context) }
    val myAvatarPath = remember { UserProfileManager.getAvatarPath(context) }
    var showChapterDrawer by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    var previousChapterIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.chapterIndex, state.isLoading) {
        if (!state.isLoading && state.sentences.isNotEmpty() && state.chapterIndex != previousChapterIndex) {
            val targetIndex = if (previousChapterIndex == -1) {
                state.savedSentenceIndex.coerceIn(0, state.sentences.lastIndex)
            } else {
                0
            }
            Log.d(TAG, "scrollTo: chapterIndex=${state.chapterIndex}, targetIndex=$targetIndex")
            listState.scrollToItem(targetIndex)
            previousChapterIndex = state.chapterIndex
        }
    }

    var lastSavedIndex by remember { mutableIntStateOf(-1) }
    var lastSavedChapter by remember { mutableIntStateOf(-1) }

    LaunchedEffect(listState.firstVisibleItemIndex, state.chapterIndex) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentChapter = state.chapterIndex

        if (currentIndex >= 0 && state.sentences.isNotEmpty()
            && (currentIndex != lastSavedIndex || currentChapter != lastSavedChapter)
        ) {
            viewModel.saveProgress(
                sentenceIndex = currentIndex,
                chapterIndex = currentChapter,
            )
            lastSavedIndex = currentIndex
            lastSavedChapter = currentChapter
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            WeChatTitleBar(
                title = state.bookTitle.ifEmpty { "聊天" },
                onBack = { onBack() },
                onMore = { showChapterDrawer = true },
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
                        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                    ) {
                        item(key = "chapter_time") {
                            ChatTimeLabel(text = "昨天 17:54")
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
                                selfName = myName,
                                otherName = state.bookTitle.ifEmpty { "书籍" },
                                fontSize = 16,
                                selfAvatarPath = myAvatarPath,
                                otherAvatarPath = state.coverPath,
                                selfAvatar = R.drawable.avatar_mom,
                                otherAvatar = R.drawable.avatar_novel,
                            )
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }
                }
            }

            WeChatChatInputBar(
                isFirstChapter = state.isFirstChapter,
                isLastChapter = state.isLastChapter,
                isLoading = state.isLoading,
                onPrevChapter = { viewModel.prevChapter() },
                onNextChapter = { viewModel.nextChapter() },
            )
        }

        if (showChapterDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable { showChapterDrawer = false },
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End,
            ) {
                ChapterDrawer(
                    chapters = state.chapterList,
                    onChapterClick = { chapterIndex ->
                        showChapterDrawer = false
                        viewModel.jumpToChapter(chapterIndex)
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(318.dp)
                        .clickable(enabled = false) {},
                )
            }
        }
    }
}

@Composable
private fun WeChatChatInputBar(
    isFirstChapter: Boolean,
    isLastChapter: Boolean,
    isLoading: Boolean,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
) {
    val colors = WechatTheme.colors
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colors.chatInputBg)
                .padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundInputIcon(
                type = InputIconType.Voice,
                modifier = Modifier
                    .size(31.dp)
                    .clickable(enabled = !isFirstChapter && !isLoading) { onPrevChapter() },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(Color.White, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                MicIcon(Modifier.size(22.dp), Color(0xFF666666))
            }

            Spacer(modifier = Modifier.width(8.dp))

            RoundInputIcon(
                type = InputIconType.Smile,
                modifier = Modifier
                    .size(31.dp)
                    .clickable {
                        try {
                            val wechatIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(wechatIntent)
                        } catch (explicitError: Exception) {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.mm")
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(launchIntent)
                            } else {
                                Log.w(TAG, "Unable to launch WeChat", explicitError)
                                Toast.makeText(context, "未安装微信", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
            )
            Spacer(modifier = Modifier.width(8.dp))
            RoundInputIcon(
                type = InputIconType.Plus,
                modifier = Modifier
                    .size(31.dp)
                    .clickable(enabled = !isLastChapter && !isLoading) { onNextChapter() },
            )
        }
    }
}

private enum class InputIconType { Voice, Smile, Plus }

@Composable
private fun RoundInputIcon(
    type: InputIconType,
    modifier: Modifier = Modifier,
) {
    val color = Color(0xFF111111)
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.035f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(
            color,
            radius = size.minDimension * 0.43f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = stroke,
        )
        when (type) {
            InputIconType.Voice -> {
                drawPath(Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.42f)
                    quadraticBezierTo(size.width * 0.35f, size.height * 0.50f, size.width * 0.28f, size.height * 0.58f)
                }, color, style = stroke)
                drawPath(Path().apply {
                    moveTo(size.width * 0.42f, size.height * 0.35f)
                    quadraticBezierTo(size.width * 0.54f, size.height * 0.50f, size.width * 0.42f, size.height * 0.65f)
                }, color, style = stroke)
                drawPath(Path().apply {
                    moveTo(size.width * 0.55f, size.height * 0.25f)
                    quadraticBezierTo(size.width * 0.73f, size.height * 0.50f, size.width * 0.55f, size.height * 0.75f)
                }, color, style = stroke)
            }
            InputIconType.Smile -> {
                drawCircle(color, radius = size.width * 0.055f, center = Offset(size.width * 0.37f, size.height * 0.38f))
                drawCircle(color, radius = size.width * 0.055f, center = Offset(size.width * 0.63f, size.height * 0.38f))
                drawPath(Path().apply {
                    moveTo(size.width * 0.29f, size.height * 0.59f)
                    quadraticBezierTo(size.width * 0.50f, size.height * 0.76f, size.width * 0.71f, size.height * 0.59f)
                }, color, style = stroke)
            }
            InputIconType.Plus -> {
                drawLine(
                    color,
                    Offset(size.width * 0.30f, size.height * 0.50f),
                    Offset(size.width * 0.70f, size.height * 0.50f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color,
                    Offset(size.width * 0.50f, size.height * 0.30f),
                    Offset(size.width * 0.50f, size.height * 0.70f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun MicIcon(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.36f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.28f, size.height * 0.50f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.14f),
            style = stroke,
        )
        drawPath(Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.42f)
            quadraticBezierTo(size.width * 0.24f, size.height * 0.76f, size.width * 0.50f, size.height * 0.76f)
            quadraticBezierTo(size.width * 0.76f, size.height * 0.76f, size.width * 0.78f, size.height * 0.42f)
        }, color, style = stroke)
        drawLine(color, Offset(size.width * 0.50f, size.height * 0.76f), Offset(size.width * 0.50f, size.height * 0.92f), strokeWidth = 2.4f, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.36f, size.height * 0.92f), Offset(size.width * 0.64f, size.height * 0.92f), strokeWidth = 2.4f, cap = StrokeCap.Round)
    }
}
