package com.novelwechat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class WeChatColorScheme(
    val green: Color,
    val greenDark: Color,
    val bubbleGreen: Color,
    val bubbleWhite: Color,
    val background: Color,
    val chatBackground: Color,
    val tabBarBackground: Color,
    val tabSelected: Color,
    val tabUnselected: Color,
    val titleBar: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val redBadge: Color,
    val chatInputBg: Color,
    val searchBarBg: Color,
    val listItemBg: Color,
)

val LocalWeChatColors = staticCompositionLocalOf {
    WeChatColorScheme(
        green = Color.Black,
        greenDark = Color.Black,
        bubbleGreen = Color.Black,
        bubbleWhite = Color.Black,
        background = Color.Black,
        chatBackground = Color.Black,
        tabBarBackground = Color.Black,
        tabSelected = Color.Black,
        tabUnselected = Color.Black,
        titleBar = Color.Black,
        divider = Color.Black,
        textPrimary = Color.Black,
        textSecondary = Color.Black,
        textHint = Color.Black,
        redBadge = Color.Black,
        chatInputBg = Color.Black,
        searchBarBg = Color.Black,
        listItemBg = Color.Black,
    )
}

private val LightColorScheme = WeChatColorScheme(
    green = WeChatLight.Green,
    greenDark = WeChatLight.GreenDark,
    bubbleGreen = WeChatLight.BubbleGreen,
    bubbleWhite = WeChatLight.BubbleWhite,
    background = WeChatLight.Background,
    chatBackground = WeChatLight.ChatBackground,
    tabBarBackground = WeChatLight.TabBarBackground,
    tabSelected = WeChatLight.TabSelected,
    tabUnselected = WeChatLight.TabUnselected,
    titleBar = WeChatLight.TitleBar,
    divider = WeChatLight.Divider,
    textPrimary = WeChatLight.TextPrimary,
    textSecondary = WeChatLight.TextSecondary,
    textHint = WeChatLight.TextHint,
    redBadge = WeChatLight.RedBadge,
    chatInputBg = WeChatLight.ChatInputBg,
    searchBarBg = WeChatLight.SearchBarBg,
    listItemBg = WeChatLight.ListItemBg,
)

private val DarkColorScheme = WeChatColorScheme(
    green = WeChatDark.Green,
    greenDark = WeChatDark.GreenDark,
    bubbleGreen = WeChatDark.BubbleGreen,
    bubbleWhite = WeChatDark.BubbleWhite,
    background = WeChatDark.Background,
    chatBackground = WeChatDark.ChatBackground,
    tabBarBackground = WeChatDark.TabBarBackground,
    tabSelected = WeChatDark.TabSelected,
    tabUnselected = WeChatDark.TabUnselected,
    titleBar = WeChatDark.TitleBar,
    divider = WeChatDark.Divider,
    textPrimary = WeChatDark.TextPrimary,
    textSecondary = WeChatDark.TextSecondary,
    textHint = WeChatDark.TextHint,
    redBadge = WeChatDark.RedBadge,
    chatInputBg = WeChatDark.ChatInputBg,
    searchBarBg = WeChatDark.SearchBarBg,
    listItemBg = WeChatDark.ListItemBg,
)

@Composable
fun WechatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val wechatColors = if (darkTheme) DarkColorScheme else LightColorScheme

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = wechatColors.green,
            background = wechatColors.background,
            surface = wechatColors.listItemBg,
            onPrimary = Color.White,
            onBackground = wechatColors.textPrimary,
            onSurface = wechatColors.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = wechatColors.green,
            background = wechatColors.background,
            surface = wechatColors.listItemBg,
            onPrimary = Color.White,
            onBackground = wechatColors.textPrimary,
            onSurface = wechatColors.textPrimary,
        )
    }

    CompositionLocalProvider(LocalWeChatColors provides wechatColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content,
        )
    }
}

object WechatTheme {
    val colors: WeChatColorScheme
        @Composable get() = LocalWeChatColors.current
}
