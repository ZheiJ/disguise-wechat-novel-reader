package com.novelwechat.data

import com.novelwechat.data.local.AppDatabase
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.local.entity.Chapter

object BuiltinReadmeSeeder {
    private const val FILE_PATH = "__builtin_readme__"

    private val content = """
        使用教程
        1. 主页面右上角加号添加小说，目前仅支持txt格式
        2. 在“我”界面点击头像可以改头像，点击名字可以改名。请改至与你微信一致，减少被发现风险
        3. “微信”界面长按你导入的小说可以修改小说名和头像
        4. 欢迎前往https://github.com/ZheiJ/disguise-wechat-novel-reader 为我star和提出修改意见
        5. 本软件完全免费
        联系作者：junjieh761@gmail.com
    """.trimIndent()

    suspend fun seed(database: AppDatabase) {
        val bookDao = database.bookDao()
        if (bookDao.countActiveByFilePath(FILE_PATH) > 0) return

        val now = System.currentTimeMillis()
        val bookId = bookDao.insert(
            Book(
                title = "读我",
                author = "使用教程",
                filePath = FILE_PATH,
                fileType = "txt",
                totalChapters = 1,
                addedTime = now,
                lastReadTime = now,
                isPinned = true,
            )
        )

        database.chapterDao().insertAll(
            listOf(
                Chapter(
                    bookId = bookId,
                    chapterIndex = 0,
                    title = "使用教程",
                    content = content,
                    sentenceCount = 6,
                )
            )
        )
    }
}
