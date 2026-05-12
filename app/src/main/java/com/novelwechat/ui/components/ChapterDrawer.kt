package com.novelwechat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    Column(modifier = modifier.fillMaxSize().background(colors.listItemBg)) {
        Text(
            text = "章节目录",
            color = colors.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp),
        )
        Divider(color = colors.divider, thickness = 0.5.dp)

        LazyColumn {
            items(chapters) { chapter ->
                val textColor = if (chapter.isCurrent) colors.green else colors.textPrimary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChapterClick(chapter.index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = chapter.title,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = if (chapter.isCurrent) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    if (chapter.isCurrent) {
                        Text(
                            text = "▸",
                            color = colors.green,
                            fontSize = 14.sp,
                        )
                    }
                }
                Divider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}
