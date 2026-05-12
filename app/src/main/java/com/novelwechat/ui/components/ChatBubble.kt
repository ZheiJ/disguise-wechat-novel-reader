package com.novelwechat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.theme.WechatTheme

@Composable
fun ChatBubble(
    text: String,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    fontSize: Int = 16,
) {
    val colors = WechatTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
    ) {
        if (!isFromMe) {
            // 左侧头像（对方=绿色=旁白）
            WeChatAvatar(isFromMe = false, bgColor = Color(0xFF07C160))
            Spacer(modifier = Modifier.width(6.dp))
        }

        val bubbleColor = if (isFromMe) colors.bubbleGreen else colors.bubbleWhite
        val textColor = if (isFromMe) Color(0xFF111111) else colors.textPrimary
        val bubbleShape = if (isFromMe) {
            RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        } else {
            RoundedCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        }

        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = textColor,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize + 8).sp,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }

        if (isFromMe) {
            Spacer(modifier = Modifier.width(6.dp))
            // 右侧头像（自己=蓝色=对话）
            WeChatAvatar(isFromMe = true, bgColor = Color(0xFF576B95))
        }
    }
}

@Composable
fun WeChatAvatar(
    isFromMe: Boolean,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isFromMe) "我" else "书",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
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
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = WechatTheme.colors.textHint,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun ParagraphTimeLabel(
    paragraphIndex: Int,
    modifier: Modifier = Modifier,
) {
    val fakeHour = (9 + paragraphIndex * 7 % 12)
    val fakeMinute = paragraphIndex * 13 % 60
    val period = if (fakeHour < 12) "上午" else "下午"
    val displayHour = if (fakeHour > 12) fakeHour - 12 else fakeHour
    val timeText = "$period $displayHour:${fakeMinute.toString().padStart(2, '0')}"
    ChatTimeLabel(text = timeText, modifier = modifier)
}
