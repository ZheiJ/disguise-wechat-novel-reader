package com.novelwechat.data.parser

class TxtParser {

    companion object {
        // 匹配常见章节格式：第X章、第X节、Chapter X、楔子、序章、尾声等
        private val CHAPTER_PATTERN = Regex(
            """^\s*(第[零一二三四五六七八九十百千万〇0-9]+[章节回卷集部篇][^。！？\n]*|Chapter\s+\d+.*|第[零一二三四五六七八九十百千万〇0-9]+[章节回卷集部篇]\s*|楔子|序[章言]|引[子言]|尾声|番外|终章|后记)\s*$""",
            RegexOption.IGNORE_CASE
        )

        // 补充匹配：纯数字章节标题如 "1." "2、" "01" "第001章" 等
        private val SIMPLE_CHAPTER_PATTERN = Regex(
            """^\s*\d+[.、．\s]?\s*$""",
            RegexOption.IGNORE_CASE
        )

        // 匹配 "第一百二十三章" 等中文大数字
        private val CHINESE_NUM_CHAPTER = Regex(
            """^\s*第[零一二三四五六七八九十百千万亿〇0-9]+[章节回卷集部篇]\s*$""",
            RegexOption.IGNORE_CASE
        )
    }

    fun parse(content: String, fileName: String): ParseResult {
        val lines = content.lines()
        val chapterBreaks = mutableListOf<ChapterInfo>()
        var bookTitle = fileName.removeSuffix(".txt")

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (CHAPTER_PATTERN.matches(line) || CHINESE_NUM_CHAPTER.matches(line) || SIMPLE_CHAPTER_PATTERN.matches(line)) {
                chapterBreaks.add(ChapterInfo(line, i))
            }
        }

        // 如果没有找到章节，整本书作为一章
        if (chapterBreaks.isEmpty()) {
            return ParseResult(
                title = bookTitle,
                author = "未知",
                chapters = listOf(ParsedChapter(0, bookTitle, content.trim()))
            )
        }

        val chapters = mutableListOf<ParsedChapter>()

        // 如果第一章之前有内容，作为"序言"或"封面"
        if (chapterBreaks[0].startIndex > 0) {
            val preContent = lines.subList(0, chapterBreaks[0].startIndex)
                .joinToString("\n").trim()
            if (preContent.isNotEmpty()) {
                chapters.add(ParsedChapter(0, "序", preContent))
            }
        }

        for (i in chapterBreaks.indices) {
            val startLine = chapterBreaks[i].startIndex
            val endLine = if (i + 1 < chapterBreaks.size) chapterBreaks[i + 1].startIndex else lines.size
            val chapterContent = lines.subList(startLine + 1, endLine)
                .joinToString("\n").trim()
            val title = chapterBreaks[i].title.trim()
            chapters.add(ParsedChapter(chapters.size, title, chapterContent))
        }

        return ParseResult(
            title = bookTitle,
            author = "未知",
            chapters = chapters,
        )
    }
}
