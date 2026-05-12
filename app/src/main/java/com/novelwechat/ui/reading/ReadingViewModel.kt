package com.novelwechat.ui.reading

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.novelwechat.data.local.entity.Book
import com.novelwechat.data.local.entity.Chapter
import com.novelwechat.data.local.entity.ReadProgress
import com.novelwechat.data.repository.BookRepository
import com.novelwechat.data.repository.ReadProgressRepository
import com.novelwechat.ui.components.ChapterItem
import com.novelwechat.util.SentenceSplitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ReadingViewModel"

data class BubbleUiState(
    val bookTitle: String = "",
    val author: String = "",
    val coverPath: String? = null,
    val chapterTitle: String = "",
    val chapterIndex: Int = 0,
    val totalChapters: Int = 0,
    val sentences: List<SentenceSplitter.BubbleSentence> = emptyList(),
    val chapterList: List<ChapterItem> = emptyList(),
    val fontSize: Int = 16,
    val isLoading: Boolean = true,
    val isLastChapter: Boolean = false,
    val isFirstChapter: Boolean = true,
    val savedSentenceIndex: Int = 0,
)

class ReadingViewModel(
    private val bookRepo: BookRepository,
    private val progressRepo: ReadProgressRepository,
) : ViewModel() {

    private var currentBookId: Long = -1

    private val _state = MutableStateFlow(BubbleUiState())
    val state: StateFlow<BubbleUiState> = _state.asStateFlow()

    private var loadChapterJob: Job? = null

    // 章节内容缓存：key=章节索引, value=(标题, 句子列表)
    // 最多保留20章
    private val chapterCache = LinkedHashMap<Int, Pair<String, List<SentenceSplitter.BubbleSentence>>>(
        20, 0.75f, false
    )

    fun loadBook(bookId: Long) {
        currentBookId = bookId
        viewModelScope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(isLoading = true)
            val book = bookRepo.getBookById(bookId) ?: run {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }

            // 加载章节列表（只加载标题，不加载内容，很快）
            loadChapterList(book.totalChapters)

            // 恢复进度
            val progress = progressRepo.getProgress(bookId)
            val startChapter = progress?.chapterIndex ?: 0
            val startSentence = progress?.sentenceIndex ?: 0

            _state.value = _state.value.copy(
                bookTitle = book.title,
                author = book.author,
                coverPath = book.coverPath,
                totalChapters = book.totalChapters,
                savedSentenceIndex = startSentence,
            )
            loadChapter(startChapter)
        }
    }

    /**
     * 加载章节列表。只查章节标题（不查content列），即使章节很多也很快。
     */
    private suspend fun loadChapterList(totalChapters: Int) {
        val items = mutableListOf<ChapterItem>()
        for (i in 0 until minOf(totalChapters, 50)) {
            val chapter = progressRepo.getChapter(currentBookId, i)
            items.add(
                ChapterItem(
                    index = i,
                    title = chapter?.title ?: "第${i + 1}章",
                    isCurrent = false,
                )
            )
        }
        // 如果超过50章，剩余的用占位标题
        if (totalChapters > 50) {
            for (i in 50 until totalChapters) {
                items.add(
                    ChapterItem(
                        index = i,
                        title = "第${i + 1}章",
                        isCurrent = false,
                    )
                )
            }
        }
        _state.value = _state.value.copy(chapterList = items)
    }

    fun loadChapter(index: Int) {
        Log.d(
            TAG,
            "loadChapter called: index=$index, currentChapterIndex=${_state.value.chapterIndex}, totalChapters=${_state.value.totalChapters}"
        )
        loadChapterJob?.cancel()
        loadChapterJob = viewModelScope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(
                chapterTitle = "",
                sentences = emptyList(),
                isLoading = true,
            )

            // 先查缓存
            val cached = chapterCache[index]
            if (cached != null) {
                val (title, sentences) = cached
                _state.value = _state.value.copy(
                    chapterTitle = title,
                    chapterIndex = index,
                    sentences = sentences,
                    isLoading = false,
                    isFirstChapter = index == 0,
                    isLastChapter = index >= _state.value.totalChapters - 1,
                    chapterList = _state.value.chapterList.map { it.copy(isCurrent = it.index == index) },
                )
                Log.d(TAG, "loadChapter from cache: index=$index, title=$title")
                bookRepo.updateLastReadTime(currentBookId)
                // 预加载前后章节，保持20章缓存
                prefetchAround(index)
                return@launch
            }

            val chapter = withContext(Dispatchers.IO) {
                progressRepo.getChapter(currentBookId, index)
            }
            if (chapter == null) {
                Log.w(TAG, "loadChapter: chapter not found, index=$index")
                _state.value = _state.value.copy(
                    isLoading = false,
                    isFirstChapter = index == 0,
                    isLastChapter = index >= _state.value.totalChapters - 1,
                )
                return@launch
            }

            val sentences = withContext(Dispatchers.Default) {
                SentenceSplitter.split(chapter.content)
            }
            chapterCache[index] = Pair(chapter.title, sentences)
            _state.value = _state.value.copy(
                chapterTitle = chapter.title,
                chapterIndex = index,
                sentences = sentences,
                isLoading = false,
                isFirstChapter = index == 0,
                isLastChapter = index >= _state.value.totalChapters - 1,
                chapterList = _state.value.chapterList.map { it.copy(isCurrent = it.index == index) },
            )
            Log.d(
                TAG,
                "loadChapter completed: index=$index, chapterTitle=${chapter.title}, sentencesCount=${sentences.size}"
            )
            bookRepo.updateLastReadTime(currentBookId)
            // 预加载前后章节
            prefetchAround(index)
        }
    }

    /**
     * 预加载当前章节周围的章节，确保缓存中始终有20章。
     * 优先加载后面的章节（因为用户通常向后翻）。
     */
    private fun prefetchAround(currentIndex: Int) {
        val total = _state.value.totalChapters
        // 前面留5章，后面留14章 = 20章（含当前章）
        val rangeStart = maxOf(0, currentIndex - 5)
        val rangeEnd = minOf(total - 1, currentIndex + 14)

        for (i in rangeStart..rangeEnd) {
            if (i == currentIndex) continue // 当前章节已加载
            if (chapterCache.containsKey(i)) continue // 已在缓存中
            prefetchChapter(i)
        }

        // 清理超出范围的缓存
        val toRemove = chapterCache.keys.filter { it < rangeStart || it > rangeEnd }
        toRemove.forEach { chapterCache.remove(it) }
    }

    /**
     * 后台预加载单个章节，存入缓存
     */
    private fun prefetchChapter(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (chapterCache.containsKey(index)) return@launch
            val chapter = progressRepo.getChapter(currentBookId, index) ?: return@launch
            val sentences = SentenceSplitter.split(chapter.content)
            synchronized(chapterCache) {
                chapterCache[index] = Pair(chapter.title, sentences)
            }
            Log.d(TAG, "prefetched chapter: index=$index")
        }
    }

    /**
     * 保存阅读进度。参数使用调用时刻的快照值，避免竞态条件。
     */
    fun saveProgress(sentenceIndex: Int, chapterIndex: Int) {
        Log.d(TAG, "saveProgress called: chapterIndex=$chapterIndex, sentenceIndex=$sentenceIndex")
        viewModelScope.launch(Dispatchers.IO) {
            progressRepo.saveProgress(
                ReadProgress(
                    bookId = currentBookId,
                    chapterIndex = chapterIndex,
                    sentenceIndex = sentenceIndex,
                    lastReadTime = System.currentTimeMillis(),
                )
            )
        }
    }

    fun nextChapter() {
        val s = _state.value
        val targetIndex = s.chapterIndex + 1
        Log.d(
            TAG,
            "nextChapter called: current=${s.chapterIndex}, target=$targetIndex, isLast=${s.isLastChapter}, total=${s.totalChapters}"
        )
        if (!s.isLastChapter && targetIndex < s.totalChapters) {
            loadChapter(targetIndex)
        } else {
            Log.w(TAG, "nextChapter BLOCKED: isLast=${s.isLastChapter}, total=${s.totalChapters}")
        }
    }

    fun prevChapter() {
        val s = _state.value
        val targetIndex = s.chapterIndex - 1
        Log.d(
            TAG,
            "prevChapter called: current=${s.chapterIndex}, target=$targetIndex, isFirst=${s.isFirstChapter}, total=${s.totalChapters}"
        )
        if (!s.isFirstChapter && targetIndex >= 0) {
            loadChapter(targetIndex)
        } else {
            Log.w(TAG, "prevChapter BLOCKED: isFirst=${s.isFirstChapter}, total=${s.totalChapters}")
        }
    }

    fun jumpToChapter(index: Int) {
        val total = _state.value.totalChapters
        if (index in 0 until total) {
            loadChapter(index)
        }
    }

    fun setFontSize(size: Int) {
        _state.value = _state.value.copy(fontSize = size)
    }
}

class ReadingViewModelFactory(
    private val bookRepo: BookRepository,
    private val progressRepo: ReadProgressRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReadingViewModel(bookRepo, progressRepo) as T
    }
}