package com.novelwechat.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.theme.WechatTheme

@Composable
fun WeChatTitleBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    avatarPath: String? = null,
    subtitle: String? = null,
) {
    val colors = WechatTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.titleBar)
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Text(
                    text = "‹",
                    color = colors.green,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.clickable { onBack() }.padding(end = 8.dp),
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
            if (avatarPath != null) {
                val avatarBitmap = remember(avatarPath) {
                    try {
                        val file = java.io.File(avatarPath)
                        if (file.exists()) BitmapFactory.decodeFile(avatarPath)?.asImageBitmap() else null
                    } catch (e: Exception) { null }
                }
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)).background(colors.green)) {
                    if (avatarBitmap != null) {
                        Image(bitmap = avatarBitmap, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(text = title.take(1), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (onMore != null) {
                Text(text = "⋯", color = colors.textSecondary, fontSize = 24.sp, modifier = Modifier.clickable { onMore() }.padding(start = 12.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}

@Composable
fun WeChatSearchBar(placeholder: String, onSearch: (String) -> Unit = {}) {
    val colors = WechatTheme.colors
    Row(modifier = Modifier.fillMaxWidth().background(colors.searchBarBg).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f).background(colors.background, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = placeholder, color = colors.textHint, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeChatChatListItem(title: String, subtitle: String, timeText: String, unreadCount: Int = 0, isPinned: Boolean = false, coverPath: String? = null, onClick: () -> Unit, onLongPress: () -> Unit = {}, modifier: Modifier = Modifier) {
    val colors = WechatTheme.colors
    val bgColor = if (isPinned) colors.searchBarBg else colors.listItemBg
    val avatarBitmap = remember(coverPath) {
        coverPath?.let { path ->
            try { val file = java.io.File(path); if (file.exists()) BitmapFactory.decodeFile(path)?.asImageBitmap() else null } catch (e: Exception) { null }
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().background(bgColor).combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(colors.green), contentAlignment = Alignment.Center) {
                if (avatarBitmap != null) Image(bitmap = avatarBitmap, contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text(text = title.take(1), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = colors.textSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = timeText, color = colors.textHint, fontSize = 12.sp)
                if (unreadCount > 0) Box(modifier = Modifier.padding(top = 4.dp).background(Color.Red, RoundedCornerShape(10.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(text = unreadCount.toString(), color = Color.White, fontSize = 10.sp) }
            }
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}

@Composable
fun WeChatContactItem(name: String, subtitle: String? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = WechatTheme.colors
    Row(modifier = modifier.fillMaxWidth().background(colors.listItemBg).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(colors.green), contentAlignment = Alignment.Center) {
            Text(text = name.take(1), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = name, color = colors.textPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(text = "›", color = colors.textHint, fontSize = 20.sp)
    }
}

@Composable
fun WeChatSectionHeader(letter: String, modifier: Modifier = Modifier) {
    val colors = WechatTheme.colors
    Text(text = letter, color = colors.textSecondary, fontSize = 14.sp, modifier = modifier.fillMaxWidth().background(colors.background).padding(horizontal = 16.dp, vertical = 8.dp))
}
