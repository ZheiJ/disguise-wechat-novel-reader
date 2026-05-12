package com.novelwechat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val coverPath: String? = null,
    val filePath: String,
    val fileType: String, // "txt" or "epub"
    val totalChapters: Int = 0,
    val addedTime: Long,
    val lastReadTime: Long = 0,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val isDeleted: Boolean = false,
)
