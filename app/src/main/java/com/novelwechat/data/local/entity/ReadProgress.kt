package com.novelwechat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "read_progress",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReadProgress(
    @PrimaryKey
    val bookId: Long,
    val chapterIndex: Int = 0,
    val sentenceIndex: Int = 0,
    val scrollOffset: Int = 0,
    val lastReadTime: Long = 0,
)
