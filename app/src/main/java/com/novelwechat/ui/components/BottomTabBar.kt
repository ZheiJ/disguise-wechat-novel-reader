package com.novelwechat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.theme.WechatTheme

data class TabItem(
    val label: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
    val badge: Int = 0,
)

@Composable
fun WeChatBottomTabBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WechatTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .background(colors.tabBarBackground)
                .padding(bottom = 3.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val tintColor = if (selected) colors.tabSelected else Color.Black

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            WeChatTabIcon(
                                index = index,
                                selected = selected,
                                tint = tintColor,
                                modifier = Modifier.size(27.dp),
                            )
                            if (tab.badge > 0) {
                                BadgeCount(count = tab.badge)
                            } else if (index == 2) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-1).dp)
                                        .size(9.dp)
                                        .background(colors.redBadge, CircleShape),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = if (selected) colors.tabSelected else colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCount(count: Int) {
    val colors = WechatTheme.colors
    Box(
        modifier = Modifier
            .offset(x = 18.dp, y = (-4).dp)
            .background(colors.redBadge, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = if (count > 9) 7.dp else 0.dp)
            .size(if (count > 9) 30.dp else 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (count > 9) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun WeChatTabIcon(
    index: Int,
    selected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (index) {
            0 -> {
                val green = Color(0xFF21C063)
                val red = Color(0xFFFA5151)
                drawOval(
                    color = green,
                    topLeft = Offset(size.width * 0.02f, size.height * 0.22f),
                    size = Size(size.width * 0.58f, size.height * 0.48f),
                )
                drawPath(Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.66f)
                    lineTo(size.width * 0.15f, size.height * 0.88f)
                    lineTo(size.width * 0.34f, size.height * 0.72f)
                }, green)
                drawRoundRect(
                    color = red,
                    topLeft = Offset(size.width * 0.36f, size.height * 0.05f),
                    size = Size(size.width * 0.60f, size.height * 0.46f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.22f),
                )
                drawCircle(Color.White, radius = size.width * 0.025f, center = Offset(size.width * 0.58f, size.height * 0.28f))
                drawCircle(Color.White, radius = size.width * 0.025f, center = Offset(size.width * 0.68f, size.height * 0.28f))
                drawCircle(Color.White, radius = size.width * 0.025f, center = Offset(size.width * 0.78f, size.height * 0.28f))
            }
            1 -> {
                drawCircle(tint, radius = size.width * 0.13f, center = Offset(size.width * 0.42f, size.height * 0.30f), style = stroke)
                drawPath(Path().apply {
                    moveTo(size.width * 0.20f, size.height * 0.78f)
                    cubicTo(size.width * 0.25f, size.height * 0.52f, size.width * 0.60f, size.height * 0.52f, size.width * 0.66f, size.height * 0.78f)
                }, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.40f), Offset(size.width * 0.90f, size.height * 0.40f), strokeWidth = 3.2f, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.55f), Offset(size.width * 0.86f, size.height * 0.55f), strokeWidth = 3.2f, cap = StrokeCap.Round)
            }
            2 -> {
                drawCircle(tint, radius = size.width * 0.38f, center = Offset(size.width * 0.50f, size.height * 0.50f), style = stroke)
                drawPath(Path().apply {
                    moveTo(size.width * 0.38f, size.height * 0.66f)
                    lineTo(size.width * 0.62f, size.height * 0.32f)
                    lineTo(size.width * 0.55f, size.height * 0.58f)
                    close()
                }, tint, style = stroke)
            }
            else -> {
                drawCircle(tint, radius = size.width * 0.13f, center = Offset(size.width * 0.50f, size.height * 0.30f), style = stroke)
                drawPath(Path().apply {
                    moveTo(size.width * 0.22f, size.height * 0.80f)
                    cubicTo(size.width * 0.28f, size.height * 0.54f, size.width * 0.72f, size.height * 0.54f, size.width * 0.78f, size.height * 0.80f)
                }, tint, style = stroke)
            }
        }
    }
}
