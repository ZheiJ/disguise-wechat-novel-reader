package com.novelwechat.data.parser

data class ChapterInfo(
    val title: String,
    val startIndex: Int,
)

data class ParseResult(
    val title: String,
    val author: String,
    val chapters: List<ParsedChapter>,
)

data class ParsedChapter(
    val index: Int,
    val title: String,
    val content: String,
)
