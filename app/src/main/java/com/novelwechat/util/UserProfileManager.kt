package com.novelwechat.util

import android.content.Context

object UserProfileManager {

    private const val PREFS_NAME = "user_profile"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_AVATAR_PATH = "avatar_path"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getNickname(context: Context): String {
        return prefs(context).getString(KEY_NICKNAME, "我") ?: "我"
    }

    fun setNickname(context: Context, nickname: String) {
        prefs(context).edit().putString(KEY_NICKNAME, nickname).apply()
    }

    fun getAvatarPath(context: Context): String? {
        return prefs(context).getString(KEY_AVATAR_PATH, null)
    }

    fun setAvatarPath(context: Context, path: String) {
        prefs(context).edit().putString(KEY_AVATAR_PATH, path).apply()
    }
}
