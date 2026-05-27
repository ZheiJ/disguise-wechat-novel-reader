package com.novelwechat.ui.components

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.R
import com.novelwechat.ui.theme.WechatTheme

@Composable
fun WeChatTitleBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    avatarPath: String? = null,
    subtitle: String? = null,
    showHomeActions: Boolean = false,
) {
    val colors = WechatTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(colors.titleBar),
        ) {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(44.dp)
                        .clickable { onBack() }
                        .padding(start = 16.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WeChatBackIcon(Modifier.size(30.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = "256",
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = if (onBack != null) 150.dp else 190.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showHomeActions) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索",
                        tint = colors.textPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onSearch?.invoke() },
                    )
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "添加",
                        tint = colors.textPrimary,
                        modifier = Modifier
                            .size(27.dp)
                            .clickable { onAdd?.invoke() },
                    )
                } else if (onMore != null) {
                    Text(
                        text = "...",
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.clickable { onMore() },
                    )
                }
            }
        }
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
    }
}

@Composable
fun WeChatSearchBar(placeholder: String, onSearch: (String) -> Unit = {}) {
    val colors = WechatTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.searchBarBg)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = placeholder, color = colors.textHint, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeChatChatListItem(
    title: String,
    subtitle: String,
    timeText: String,
    unreadCount: Int = 0,
    isPinned: Boolean = false,
    coverPath: String? = null,
    @DrawableRes fallbackAvatar: Int = R.drawable.avatar_mom,
    muted: Boolean = false,
    redPrefix: String? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = WechatTheme.colors
    val bgColor = if (isPinned) Color(0xFFEDEDED) else colors.listItemBg

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(bgColor)
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                .padding(start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                WeChatAvatarImage(
                    coverPath = coverPath,
                    fallbackAvatar = fallbackAvatar,
                    contentDescription = title,
                    modifier = Modifier.size(44.dp),
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-5).dp)
                            .size(12.dp)
                            .background(colors.redBadge, CircleShape),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = if (title == "微信游戏") Color(0xFF576B95) else colors.textPrimary,
                        fontSize = 17.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = timeText,
                        color = colors.textHint,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (redPrefix != null) {
                        Text(
                            text = redPrefix,
                            color = Color(0xFFE05A5A),
                            fontSize = 14.sp,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = subtitle,
                        color = colors.textHint,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (muted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        WeChatMutedIcon(Modifier.size(19.dp))
                    }
                }
            }
        }
        HorizontalDivider(
            color = colors.divider,
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 70.dp),
        )
    }
}

@Composable
fun WeChatContactItem(
    name: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WechatTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.listItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.green),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = name.take(1), color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = name, color = colors.textPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun WeChatSectionHeader(letter: String, modifier: Modifier = Modifier) {
    val colors = WechatTheme.colors
    Text(
        text = letter,
        color = colors.textSecondary,
        fontSize = 14.sp,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
fun WeChatAvatarImage(
    coverPath: String?,
    @DrawableRes fallbackAvatar: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val avatarBitmap: ImageBitmap? = remember(coverPath) {
        coverPath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists()) BitmapFactory.decodeFile(path)?.asImageBitmap() else null
            } catch (_: Exception) {
                null
            }
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFE5E5E5)),
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(fallbackAvatar),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun WeChatBackIcon(modifier: Modifier = Modifier) {
    val color = WechatTheme.colors.textPrimary
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 4.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.68f, size.height * 0.12f)
            lineTo(size.width * 0.25f, size.height * 0.50f)
            lineTo(size.width * 0.68f, size.height * 0.88f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun WeChatMutedIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = Color(0xFFB6B6B6)
        val stroke = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(c, Offset(size.width * 0.22f, size.height * 0.48f), Offset(size.width * 0.42f, size.height * 0.48f), strokeWidth = 2.4f)
        drawLine(c, Offset(size.width * 0.42f, size.height * 0.48f), Offset(size.width * 0.62f, size.height * 0.30f), strokeWidth = 2.4f)
        drawLine(c, Offset(size.width * 0.62f, size.height * 0.30f), Offset(size.width * 0.62f, size.height * 0.70f), strokeWidth = 2.4f)
        drawLine(c, Offset(size.width * 0.42f, size.height * 0.48f), Offset(size.width * 0.62f, size.height * 0.70f), strokeWidth = 2.4f)
        drawLine(c, Offset(size.width * 0.18f, size.height * 0.16f), Offset(size.width * 0.84f, size.height * 0.84f), strokeWidth = 2.4f, cap = StrokeCap.Round)
    }
}
