package com.biblelib.feature.reader.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.ScriptureItemEntity
import com.biblelib.core.casting.data.CastingRepo
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.feature.reader.home.utils.NotesNavRequest
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.utils.ScrollTarget
import com.biblelib.feature.reader.home.viewmodel.controller.AnnotationController
import com.biblelib.feature.reader.home.viewmodel.controller.ContentController
import com.biblelib.feature.reader.home.viewmodel.controller.DownloadController
import com.biblelib.feature.reader.home.viewmodel.controller.ParallelBibleController
import com.biblelib.feature.reader.home.viewmodel.controller.PreferencesController
import com.biblelib.feature.reader.home.viewmodel.controller.ReadingProgressController
import com.biblelib.feature.reader.home.viewmodel.controller.ScriptureQueueController
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bibleRepo: BibleRepo,
    private val trackingRepo: TrackingRepo,
    private val prefsRepo: PrefsRepo,
    private val annotationRepo: AnnotationRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val castingRepo: CastingRepo,
    @ApplicationContext context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val parallelBible = ParallelBibleController(bibleRepo, prefsRepo, _uiState)
    private val readingProgress = ReadingProgressController(
        prefsRepo, trackingRepo, scriptureQueueRepo, castingRepo, viewModelScope, _uiState,
    )
    private val content = ContentController(
        bibleRepo, prefsRepo, annotationRepo, parallelBible, readingProgress,
        viewModelScope, _uiState,
    )
    private val downloads = DownloadController(
        bibleRepo, context, viewModelScope, _uiState,
        onBibleDownloaded = { abbr ->
            // If the bible that just finished downloading in the background is the one the
            // reader is (trying to) show, and nothing loaded yet — e.g. the user was sent
            // straight to the reader while their primary Bible was still fetching — retry
            // loading its content now instead of leaving the "please wait" error showing.
            val current = _uiState.value
            if (abbr == current.activeBibleAbbr && current.books.isEmpty()) {
                viewModelScope.launch {
                    content.loadBooks(abbr, prefsRepo.lastBookId, prefsRepo.lastChapterId)
                }
            }
        },
    )
    private val annotations = AnnotationController(annotationRepo, viewModelScope, _uiState)
    private val queue = ScriptureQueueController(
        scriptureQueueRepo, prefsRepo, content, viewModelScope, _uiState,
    )
    private val preferences = PreferencesController(prefsRepo, _uiState)

    init {
        queue.observeQueue()
    }

    fun initialize(
        initialBible: String,
        initialBibleAbbr: String,
        initialBookId: String,
        initialChapterId: String,
        initialVerseId: String = "",
        initialSearchQry: String = "",
    ) {
        viewModelScope.launch {
            try {
                val bibles = bibleRepo.getbibles()
                if (bibles.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No Bibles downloaded yet."
                        )
                    }
                    return@launch
                }

                val bibleName = initialBible.ifEmpty {
                    prefsRepo.lastBible.ifEmpty { bibles.first().name }
                }

                val bibleAbbr = initialBibleAbbr.ifEmpty {
                    prefsRepo.lastBibleAbbr.ifEmpty { bibles.first().abbreviation }
                }

                content.markFirstLoad()

                _uiState.update {
                    it.copy(
                        savedBibles = bibles,
                        activeBible = bibleName,
                        activeBibleAbbr = bibleAbbr,
                        fontSizeSp = prefsRepo.fontSizeSp,
                        fontFamilyId = prefsRepo.readerFontFamily,
                        readerBackgroundId = prefsRepo.readerBackground,
                        multiBibleReaderEnabled = prefsRepo.multiBibleReaderEnabled,
                    )
                }

                val scrollTarget = initialVerseId.takeIf { it.isNotEmpty() }?.let {
                    ScrollTarget(it, highlightQuery = initialSearchQry.ifEmpty { null })
                }
                content.loadBooks(
                    bibleAbbr,
                    initialBookId,
                    initialChapterId,
                    scrollTarget = scrollTarget
                )
                downloads.observeDownloads(bibles)
            } catch (e: Exception) {
                Log.e(TAG, "initialize error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun retryBibleDownload(abbr: String) = downloads.retryBibleDownload(abbr)
    fun restartBibleDownload(abbr: String) = downloads.restartBibleDownload(abbr)

    suspend fun loadVerses(abbr: String, chapter: ChapterEntity) = content.loadVerses(abbr, chapter)
    fun onVerseScrollPositionChanged(verseId: String, verseNumber: Int) =
        content.onVerseScrollPositionChanged(verseId, verseNumber)

    fun consumeRestoreVerseTarget() = content.consumeRestoreVerseTarget()
    fun navigateChapter(direction: Int) = content.navigateChapter(direction)
    fun setPrimaryBible(abbr: String) = content.setPrimaryBible(abbr)
    fun selectBook(book: BookEntity) = content.selectBook(book)
    fun selectChapter(chapter: ChapterEntity, scrollTarget: ScrollTarget? = null) =
        content.selectChapter(chapter, scrollTarget)

    fun setMultiBibleReaderEnabled(enabled: Boolean) = content.setMultiBibleReaderEnabled(enabled)

    fun jumpToQueueItem(item: ScriptureItemEntity) = queue.jumpToQueueItem(item)
    fun dismissScriptureQueue() = queue.dismissScriptureQueue()

    fun setFontSize(sp: Float) = preferences.setFontSize(sp)
    fun setFontFamily(id: String) = preferences.setFontFamily(id)
    fun setReaderBackground(id: String) = preferences.setReaderBackground(id)

    fun toggleVerseSelected(verseId: String) = annotations.toggleVerseSelected(verseId)
    fun clearSelection() = annotations.clearSelection()
    fun quickToggleBookmark(verseId: String) = annotations.quickToggleBookmark(verseId)
    fun requestNotesForVerse(verseId: String): NotesNavRequest? =
        annotations.requestNotesForVerse(verseId)

    fun consumeNotesNavRequest() = annotations.consumeNotesNavRequest()
    fun openColorPicker() = annotations.openColorPicker()
    fun dismissColorPicker() = annotations.dismissColorPicker()
    fun chooseHighlightColor(colorHex: String) = annotations.chooseHighlightColor(colorHex)
    fun cancelPendingHighlight() = annotations.cancelPendingHighlight()
    fun openNotesForSelection(): NotesNavRequest? = annotations.openNotesForSelection()
    fun confirmBookmarkOnly() = annotations.confirmBookmarkOnly()
    fun confirmBookmarkWithNotes(): NotesNavRequest? = annotations.confirmBookmarkWithNotes()
    fun refreshNotedVerses() = annotations.refreshNotedVerses()
    fun buildSelectionShareText(): String? = annotations.buildSelectionShareText()
    fun buildActiveChapterShareText(): String? = annotations.buildActiveChapterShareText()

    /** What tapping "Share" on the screenshot reminder should send — the current verse
     *  selection if one is active, otherwise the whole active chapter. */
    fun buildShareText(): String? =
        if (_uiState.value.isSelectionMode) buildSelectionShareText() else buildActiveChapterShareText()

    override fun onCleared() {
        castingRepo.publishIdle()
        super.onCleared()
    }

    companion object {
        private const val TAG = "ReaderViewModel"

        val HIGHLIGHT_COLORS = AnnotationController.HIGHLIGHT_COLORS
    }
}
