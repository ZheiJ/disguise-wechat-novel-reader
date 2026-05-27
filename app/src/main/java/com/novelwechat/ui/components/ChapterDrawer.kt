package com.novelwechat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.theme.WechatTheme

data class ChapterItem(
    val index: Int,
    val title: String,
    val isCurrent: Boolean = false,
)

@Composable
fun ChapterDrawer(
    chapters: List<ChapterItem>,
    onChapterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WechatTheme.colors
    val currentIndex = chapters.indexOfFirst { it.isCurrent }.coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentIndex - 4).coerceAtLeast(0),
    )

    LaunchedEffect(currentIndex, chapters.size) {
        if (chapters.isNotEmpty()) {
            listState.scrollToItem((currentIndex - 4).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.listItemBg),
    ) {
        Text(
            text = "章节目录",
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        )
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

        if (chapters.isEmpty()) {
            Text(
                text = "章节加载中...",
                color = colors.textSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(18.dp),
            )
            return@Column
        }

        LazyColumn(state = listState) {
            items(chapters, key = { it.index }) { chapter ->
                val textColor = if (chapter.isCurrent) colors.green else colors.textPrimary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterClick(chapter.index) }
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = chapter.title,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = if (chapter.isCurrent) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (chapter.isCurrent) {
                        Text(
                            text = "当前",
                            color = colors.green,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
                HorizontalDivider(
                    color = colors.divider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 18.dp),
                )
            }
        }
    }
}
