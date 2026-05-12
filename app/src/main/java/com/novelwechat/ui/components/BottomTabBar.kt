package com.novelwechat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.theme.WechatTheme

data class TabItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
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
        Divider(color = colors.divider, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.tabBarBackground)
                .height(56.dp)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val tintColor = if (selected) colors.tabSelected else colors.tabUnselected

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
                        Box {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label,
                                tint = tintColor,
                                modifier = Modifier.size(24.dp),
                            )
                            if (tab.badge > 0) {
                                BadgeCount(count = tab.badge)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = tintColor,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
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
            .offset(x = 10.dp, y = (-4).dp)
            .background(colors.redBadge, shape = CircleShape)
            .size(if (count > 9) 18.dp else 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (count > 9) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
