package com.biblelib.feature.reader.home.viewmodel.controller

import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.core.data.repos.BibleRepo
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.utils.ScrollTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Orchestrates loading books/chapters/verses and primary-Bible navigation.
 * Parallel-Bible fetching is delegated to [ParallelBibleController]; scroll-target
 * resolution, history, and casting are delegated to [ReadingProgressController].
 */
class ContentController(
    private val bibleRepo: BibleRepo,
    private val prefsRepo: PrefsRepo,
    private val annotationRepo: AnnotationRepo,
    private val parallelBibleController: ParallelBibleController,
    private val readingProgressController: ReadingProgressController,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    fun markFirstLoad() = readingProgressController.markFirstLoad()

    suspend fun loadBooks(
        abbr: String,
        bookId: String,
        chapterId: String,
        scrollTarget: ScrollTarget? = null,
    ) {
        val books = bibleRepo.getLocalBooks(abbr)
        if (books.isEmpty()) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "Bible data not available. Please wait for the download to complete."
                )
            }
            return
        }

        // No explicit book was requested (e.g. opening the reader from the bottom nav rather
        // than from search/history/a scripture link) — resume where the user last left off
        // instead of always falling back to the first book in the Bible.
        val resolvedBookId = bookId.ifEmpty { prefsRepo.lastBookId }
        val resolvedChapterId = chapterId.ifEmpty { prefsRepo.lastChapterId }

        val targetBook = books.find { it.id == resolvedBookId } ?: books.first()
        state.update { it.copy(books = books, activeBook = targetBook) }

        loadChapters(abbr, targetBook, resolvedChapterId, scrollTarget = scrollTarget)
    }

    private suspend fun loadChapters(
        abbr: String,
        book: BookEntity,
        chapterId: String,
        forceScrollToFirstVerse: Boolean = false,
        scrollTarget: ScrollTarget? = null,
    ) {
        val chapters = bibleRepo.getLocalChapters(abbr, book.id)
        if (chapters.isEmpty()) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "No chapters found for ${book.name}"
                )
            }
            return
        }

        val targetChapter = chapters.find { it.id == chapterId } ?: chapters.first()
        state.update { it.copy(chapters = chapters, activeChapter = targetChapter) }

        loadVerses(
            abbr,
            targetChapter,
            scrollTarget = scrollTarget,
            forceScrollToFirstVerse = forceScrollToFirstVerse,
        )
    }

    suspend fun loadVerses(
        abbr: String,
        chapter: ChapterEntity,
        scrollTarget: ScrollTarget? = null,
        forceScrollToFirstVerse: Boolean = false,
    ) {
        state.update { it.copy(isLoading = true, error = null) }
        val verses = bibleRepo.getLocalVerses(abbr, chapter.id)
        if (verses == null) {
            state.update {
                it.copy(
                    isLoading = false,
                    error = "Verses not cached. Please ensure download is complete."
                )
            }
            return
        }

        val parallelVerses = parallelBibleController.loadParallel(abbr, chapter.id)
        val multiBibleEnabled = parallelBibleController.isEnabled

        val bookmarks = annotationRepo.getBookmarksForChapter(abbr, chapter.id)
        val notedVerseIds = annotationRepo.getNotedVerseIds(abbr, chapter.id)

        val resolvedTarget = readingProgressController.resolveScrollTarget(
            scrollTarget,
            forceScrollToFirstVerse,
            verses,
        )

        state.update {
            it.copy(
                isLoading = false,
                verses = verses,
                parallelVerses = parallelVerses,
                activeChapter = chapter,
                activeBibleAbbr = abbr,
                bookmarks = bookmarks,
                notedVerseIds = notedVerseIds,
                selectedVerseIds = emptySet(),
                showColorPicker = false,
                pendingHighlightColor = null,
                multiBibleReaderEnabled = multiBibleEnabled,
                restoreVerseId = resolvedTarget?.verseId,
                highlightQuery = resolvedTarget?.highlightQuery,
            )
        }

        readingProgressController.recordVersesLoaded(
            abbr = abbr,
            chapter = chapter,
            book = state.value.activeBook,
            verses = verses,
            parallelVerses = parallelVerses,
            multiBibleEnabled = multiBibleEnabled,
            resolvedTarget = resolvedTarget,
        )
    }

    fun onVerseScrollPositionChanged(verseId: String, verseNumber: Int) =
        readingProgressController.onVerseScrollPositionChanged(verseId, verseNumber)

    fun consumeRestoreVerseTarget() = readingProgressController.consumeRestoreVerseTarget()

    fun navigateChapter(direction: Int) {
        val chapters = state.value.chapters
        val current = state.value.activeChapter ?: return
        val idx = chapters.indexOfFirst { it.id == current.id }
        val next = chapters.getOrNull(idx + direction) ?: return
        selectChapter(next)
    }

    fun setPrimaryBible(abbr: String) {
        val chapter = state.value.activeChapter ?: return
        val newName = state.value.savedBibles.find { it.abbreviation == abbr }?.name
            ?: state.value.activeBible

        prefsRepo.primaryBible = abbr
        prefsRepo.lastBible = newName
        prefsRepo.setSecondaryBibleList(prefsRepo.getSecondaryBibleList() - abbr)

        state.update { it.copy(activeBible = newName, activeBibleAbbr = abbr) }

        scope.launch {
            loadVerses(abbr, chapter)
            loadBooks(abbr, state.value.activeBook?.id ?: "", chapter.id)
        }
    }

    fun selectBook(book: BookEntity) {
        state.update { it.copy(activeBook = book, chapters = emptyList(), verses = emptyList()) }
        scope.launch {
            loadChapters(state.value.activeBibleAbbr, book, "", forceScrollToFirstVerse = true)
        }
    }

    fun selectChapter(chapter: ChapterEntity, scrollTarget: ScrollTarget? = null) {
        scope.launch {
            loadVerses(
                state.value.activeBibleAbbr,
                chapter,
                scrollTarget = scrollTarget,
                forceScrollToFirstVerse = scrollTarget == null,
            )
        }
    }

    fun setMultiBibleReaderEnabled(enabled: Boolean) {
        parallelBibleController.setEnabled(enabled)
        val chapter = state.value.activeChapter ?: return
        scope.launch { loadVerses(state.value.activeBibleAbbr, chapter) }
    }
}
