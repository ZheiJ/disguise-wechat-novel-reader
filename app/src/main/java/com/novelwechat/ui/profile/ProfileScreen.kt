package com.novelwechat.ui.profile

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.novelwechat.ui.components.WeChatTitleBar
import com.novelwechat.ui.theme.WechatTheme
import com.novelwechat.util.UserProfileManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen() {
    val colors = WechatTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nickname by remember { mutableStateOf(UserProfileManager.getNickname(context)) }
    var avatarPath by remember { mutableStateOf(UserProfileManager.getAvatarPath(context)) }
    var showEditNicknameDialog by remember { mutableStateOf(false) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { imageUri ->
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val avatarFile = File(context.filesDir, "profile_avatar.jpg")
                    inputStream?.use { input ->
                        java.io.FileOutputStream(avatarFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val path = avatarFile.absolutePath
                    UserProfileManager.setAvatarPath(context, path)
                    avatarPath = path
                    Log.d("ProfileScreen", "avatar saved to $path")
                } catch (e: Exception) {
                    Log.e("ProfileScreen", "save avatar failed", e)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeChatTitleBar(title = "我")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.listItemBg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.green)
                    .clickable { avatarPickerLauncher.launch(arrayOf("image/*")) },
                contentAlignment = Alignment.Center,
            ) {
                if (avatarPath != null) {
                    AsyncImage(
                        model = File(avatarPath!!),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = "我",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.clickable { showEditNicknameDialog = true }) {
                Text(
                    text = nickname,
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "微信号: novel_reader",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(icon = Icons.Outlined.Schedule, title = "今日阅读", subtitle = "0 分钟")
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
        ProfileMenuItem(icon = Icons.Outlined.Timer, title = "累计阅读", subtitle = "0 小时")

        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(icon = Icons.Outlined.Bookmark, title = "已读书籍", subtitle = "0 本")
        HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
        ProfileMenuItem(icon = Icons.Outlined.Bookmark, title = "我的收藏", subtitle = "0 个")

        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(icon = Icons.Outlined.Settings, title = "设置")
    }

    if (showEditNicknameDialog) {
        var newNickname by remember { mutableStateOf(nickname) }

        Dialog(onDismissRequest = { showEditNicknameDialog = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("修改昵称", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = { newNickname = it },
                        label = { Text("昵称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditNicknameDialog = false }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                if (newNickname.isNotBlank()) {
                                    UserProfileManager.setNickname(context, newNickname.trim())
                                    nickname = newNickname.trim()
                                }
                                showEditNicknameDialog = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
) {
    val colors = WechatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.listItemBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        if (subtitle != null) {
            Text(text = subtitle, color = colors.textSecondary, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "›", color = colors.textHint, fontSize = 20.sp)
    }
}
