package com.novelwechat.data.parser

import android.content.Context
import org.jsoup.Jsoup
import java.io.InputStream
import java.util.zip.ZipInputStream

class EpubParser(private val context: Context) {

    fun parse(inputStream: InputStream, fileName: String): ParseResult {
        val title = fileName.removeSuffix(".epub")
        val chapters = mutableListOf<ParsedChapter>()

        try {
            val zipStream = ZipInputStream(inputStream)
            var entry = zipStream.nextEntry

            // ePub就是一个ZIP包，找到OEBPS/或OPS/下的HTML文件
            val htmlFiles = mutableListOf<Pair<String, String>>()

            while (entry != null) {
                val name = entry.name
                if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                    val bytes = zipStream.readBytes()
                    val htmlContent = String(bytes, Charsets.UTF_8)
                    htmlFiles.add(name to htmlContent)
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }

            // 按文件名排序（简单处理，不做spine排序）
            htmlFiles.sortBy { it.first }

            for ((index, pair) in htmlFiles.withIndex()) {
                val (path, html) = pair
                val doc = Jsoup.parse(html)
                val plainText = doc.text()?.trim() ?: ""

                if (plainText.isNotEmpty()) {
                    // 尝试从<h>标签提取章节标题
                    val heading = doc.select("h1,h2,h3").firstOrNull()?.text()
                    val chapterTitle = heading ?: "第${index + 1}章"
                    chapters.add(ParsedChapter(index, chapterTitle, plainText))
                }
            }
        } catch (e: Exception) {
            // 解析失败，作为单章处理
        }

        if (chapters.isEmpty()) {
            chapters.add(ParsedChapter(0, title, "（此书内容解析失败，请尝试其他格式）"))
        }

        return ParseResult(title = title, author = "未知", chapters = chapters)
    }
}
