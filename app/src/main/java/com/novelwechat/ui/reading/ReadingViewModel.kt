package com.novelwechat.ui.reading

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
private const val FORWARD_CACHE_COUNT = 20

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
    private val chapterCache = LinkedHashMap<Int, Pair<String, List<SentenceSplitter.BubbleSentence>>>(
        FORWARD_CACHE_COUNT + 1,
        0.75f,
        false,
    )

    fun loadBook(bookId: Long) {
        currentBookId = bookId
        viewModelScope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(isLoading = true)
            val book = bookRepo.getBookById(bookId) ?: run {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }

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

            loadChapterTitles(book.totalChapters)
            loadChapter(startChapter)
        }
    }

    private suspend fun loadChapterTitles(totalChapters: Int) {
        val titlesByIndex = withContext(Dispatchers.IO) {
            progressRepo.getChapterTitles(currentBookId)
        }.associateBy { it.chapterIndex }

        val items = (0 until totalChapters).map { index ->
            ChapterItem(
                index = index,
                title = titlesByIndex[index]?.title ?: "第${index + 1}章",
                isCurrent = false,
            )
        }
        _state.value = _state.value.copy(chapterList = items)
    }

    fun loadChapter(index: Int) {
        Log.d(TAG, "loadChapter: index=$index")
        loadChapterJob?.cancel()
        loadChapterJob = viewModelScope.launch(Dispatchers.Main) {
            _state.value = _state.value.copy(
                chapterTitle = "",
                sentences = emptyList(),
                isLoading = true,
            )

            val cached = chapterCache[index]
            if (cached != null) {
                val (title, sentences) = cached
                publishChapter(index, title, sentences)
                bookRepo.updateLastReadTime(currentBookId)
                prefetchForwardWindow(index)
                return@launch
            }

            val loaded = loadAndCacheChapter(index)
            if (loaded == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isFirstChapter = index == 0,
                    isLastChapter = index >= _state.value.totalChapters - 1,
                )
                return@launch
            }

            val (title, sentences) = loaded
            publishChapter(index, title, sentences)
            bookRepo.updateLastReadTime(currentBookId)
            prefetchForwardWindow(index)
        }
    }

    private suspend fun loadAndCacheChapter(
        index: Int,
    ): Pair<String, List<SentenceSplitter.BubbleSentence>>? {
        chapterCache[index]?.let { return it }

        val chapter = withContext(Dispatchers.IO) {
            progressRepo.getChapter(currentBookId, index)
        } ?: return null

        val sentences = withContext(Dispatchers.Default) {
            SentenceSplitter.split(chapter.content)
        }
        val result = chapter.title to sentences
        synchronized(chapterCache) {
            chapterCache[index] = result
        }
        return result
    }

    private fun publishChapter(
        index: Int,
        title: String,
        sentences: List<SentenceSplitter.BubbleSentence>,
    ) {
        _state.value = _state.value.copy(
            chapterTitle = title,
            chapterIndex = index,
            sentences = sentences,
            isLoading = false,
            isFirstChapter = index == 0,
            isLastChapter = index >= _state.value.totalChapters - 1,
            chapterList = _state.value.chapterList.map { it.copy(isCurrent = it.index == index) },
        )
    }

    private fun prefetchForwardWindow(currentIndex: Int) {
        val total = _state.value.totalChapters
        if (total <= 0) return

        val rangeStart = currentIndex
        val rangeEnd = minOf(total - 1, currentIndex + FORWARD_CACHE_COUNT)

        for (i in rangeStart..rangeEnd) {
            if (i == currentIndex || chapterCache.containsKey(i)) continue
            prefetchChapter(i)
        }

        val toRemove = chapterCache.keys.filter { it < rangeStart || it > rangeEnd }
        toRemove.forEach { chapterCache.remove(it) }
    }

    private fun prefetchChapter(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (chapterCache.containsKey(index)) return@launch
            val chapter = progressRepo.getChapter(currentBookId, index) ?: return@launch
            val sentences = SentenceSplitter.split(chapter.content)
            synchronized(chapterCache) {
                chapterCache[index] = chapter.title to sentences
            }
        }
    }

    fun saveProgress(sentenceIndex: Int, chapterIndex: Int) {
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
        if (!s.isLastChapter && targetIndex < s.totalChapters) {
            loadChapter(targetIndex)
        }
    }

    fun prevChapter() {
        val s = _state.value
        val targetIndex = s.chapterIndex - 1
        if (!s.isFirstChapter && targetIndex >= 0) {
            loadChapter(targetIndex)
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
