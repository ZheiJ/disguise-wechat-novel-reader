package com.novelwechat.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novelwechat.ui.components.WeChatTitleBar
import com.novelwechat.ui.theme.WechatTheme

@Composable
fun DiscoverScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(title = "发现")

        DiscoverMenuItem(icon = Icons.Outlined.QrCode2, title = "扫一扫")
        DiscoverMenuDivider()
        DiscoverMenuItem(icon = Icons.Outlined.Search, title = "搜一搜")

        DiscoverSectionSpacer()
        DiscoverMenuItem(icon = Icons.Outlined.Circle, title = "朋友圈")
        DiscoverMenuDivider()
        DiscoverMenuItem(icon = Icons.Outlined.LiveTv, title = "视频号")

        DiscoverSectionSpacer()
        DiscoverMenuItem(icon = Icons.Outlined.Storefront, title = "购物")
        DiscoverMenuDivider()
        DiscoverMenuItem(icon = Icons.Outlined.SportsEsports, title = "游戏")

        DiscoverSectionSpacer()
        DiscoverMenuItem(icon = Icons.Outlined.Apps, title = "小程序")
    }
}

@Composable
private fun DiscoverMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
) {
    val colors = WechatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.listItemBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = colors.green,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Text(text = "›", color = colors.textHint, fontSize = 20.sp)
    }
}

@Composable
private fun DiscoverMenuDivider() {
    HorizontalDivider(
        color = WechatTheme.colors.divider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 56.dp),
    )
}

@Composable
private fun DiscoverSectionSpacer() {
    Spacer(modifier = Modifier.height(8.dp))
}
