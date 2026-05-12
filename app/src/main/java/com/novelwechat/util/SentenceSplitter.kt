package com.novelwechat.util

object SentenceSplitter {

    data class BubbleSentence(
        val text: String,
        val isDialogue: Boolean,   // true=对话(右侧绿色) false=旁白(左侧白色)
        val isParagraphStart: Boolean, // 段落开头，插入时间标签
    )

    // 对话引号字符
    private val DIALOGUE_QUOTES = setOf(
        '「', '」', '『', '』', '“', '”', '"', '"',
    )

    private val SENTENCE_END = Regex("([。！？…]+[」』”\"]*|\\n)")

    fun split(content: String): List<BubbleSentence> {
        val sentences = mutableListOf<BubbleSentence>()
        val paragraphs = content.split("\n").filter { it.isNotBlank() }

        for ((paraIndex, paragraph) in paragraphs.withIndex()) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) continue

            // 按句号等标点分割
            val parts = SENTENCE_END.split(trimmed)
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (parts.isEmpty()) continue

            for (part in parts) {
                val isDialogue = containsDialogueQuotes(part) || isDialogueLike(part)
                sentences.add(BubbleSentence(
                    text = part.trim(),
                    isDialogue = isDialogue,
                    isParagraphStart = sentences.isEmpty() ||
                        (paraIndex > 0 && sentences.last().isParagraphStart.not() && part == parts.first()),
                ))
            }

            // 在段落之间标记（用于插入伪装时间标签）
            if (paraIndex < paragraphs.size - 1 && sentences.isNotEmpty()) {
                val last = sentences.last()
                sentences[sentences.lastIndex] = last.copy(isParagraphStart = false)
            }
        }

        return sentences
    }

    private fun containsDialogueQuotes(text: String): Boolean {
        return text.any { it in DIALOGUE_QUOTES }
    }

    // 如果句子以引号开头，也算对话
    private fun isDialogueLike(text: String): Boolean {
        if (text.isEmpty()) return false
        val first = text.first()
        return first in setOf('「', '『', '"', '"', '—')
    }
}
