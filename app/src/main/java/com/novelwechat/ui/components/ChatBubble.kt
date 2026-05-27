package com.novelwechat.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.R
import com.novelwechat.ui.theme.WechatTheme

@Composable
fun ChatBubble(
    text: String,
    isFromMe: Boolean,
    selfName: String,
    otherName: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 16,
    selfAvatarPath: String? = null,
    otherAvatarPath: String? = null,
    @DrawableRes selfAvatar: Int = R.drawable.avatar_mom,
    @DrawableRes otherAvatar: Int = R.drawable.avatar_novel,
) {
    val colors = WechatTheme.colors
    val bubbleColor = if (isFromMe) colors.bubbleGreen else colors.bubbleWhite

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isFromMe) {
            WeChatAvatarImage(
                coverPath = otherAvatarPath,
                fallbackAvatar = otherAvatar,
                contentDescription = otherName,
                modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start) {
            Text(
                text = if (isFromMe) selfName else otherName,
                color = colors.textSecondary,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                modifier = Modifier.padding(
                    start = if (isFromMe) 0.dp else 2.dp,
                    end = if (isFromMe) 2.dp else 0.dp,
                    bottom = 2.dp,
                ),
            )
            Box {
                Box(
                    modifier = Modifier
                        .widthIn(max = 258.dp)
                        .background(bubbleColor, RoundedCornerShape(5.dp))
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = text,
                        style = TextStyle(
                            color = colors.textPrimary,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize + 7).sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }
                BubbleTail(
                    isFromMe = isFromMe,
                    color = bubbleColor,
                    modifier = Modifier
                        .align(if (isFromMe) Alignment.TopEnd else Alignment.TopStart)
                        .padding(top = 10.dp)
                        .size(width = 8.dp, height = 12.dp),
                )
            }
        }

        if (isFromMe) {
            Spacer(modifier = Modifier.width(8.dp))
            WeChatAvatarImage(
                coverPath = selfAvatarPath,
                fallbackAvatar = selfAvatar,
                contentDescription = selfName,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun BubbleTail(
    isFromMe: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            if (isFromMe) {
                moveTo(0f, 0f)
                lineTo(size.width, size.height * 0.50f)
                lineTo(0f, size.height)
            } else {
                moveTo(size.width, 0f)
                lineTo(0f, size.height * 0.50f)
                lineTo(size.width, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun ChatTimeLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = WechatTheme.colors.textHint,
            fontSize = 13.sp,
            lineHeight = 16.sp,
        )
    }
}

@Composable
fun ParagraphTimeLabel(
    paragraphIndex: Int,
    modifier: Modifier = Modifier,
) {
    ChatTimeLabel(text = if (paragraphIndex == 1) "昨天 17:54" else "下午 ${paragraphIndex + 1}:08", modifier = modifier)
}
