package com.biblelib.feature.reader.home.viewmodel.controller

import com.biblelib.core.casting.data.CastingRepo
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ScriptureQueueRepo
import com.biblelib.core.data.repos.TrackingRepo
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.HistoryEntity
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.utils.ScrollTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadingProgressController(
    private val prefsRepo: PrefsRepo,
    private val trackingRepo: TrackingRepo,
    private val scriptureQueueRepo: ScriptureQueueRepo,
    private val castingRepo: CastingRepo,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    private var isFirstLoad = false

    fun markFirstLoad() {
        isFirstLoad = true
    }

    fun resolveScrollTarget(
        explicitTarget: ScrollTarget?,
        forceScrollToFirstVerse: Boolean,
        verses: List<VerseDisplay>,
    ): ScrollTarget? {
        val resolved = when {
            explicitTarget != null -> explicitTarget
            forceScrollToFirstVerse -> verses.firstOrNull()?.verseId?.let { ScrollTarget(it) }
            isFirstLoad -> prefsRepo.lastVerseId.takeIf { it.isNotEmpty() }?.let { ScrollTarget(it) }
            else -> null
        }
        isFirstLoad = false
        return resolved
    }

    suspend fun recordVersesLoaded(
        abbr: String,
        chapter: ChapterEntity,
        book: BookEntity?,
        verses: List<VerseDisplay>,
        parallelVerses: Map<String, List<VerseDisplay>>,
        multiBibleEnabled: Boolean,
        resolvedTarget: ScrollTarget?,
    ) {
        prefsRepo.lastBibleAbbr = abbr
        prefsRepo.lastBookId = chapter.bookId
        prefsRepo.lastChapterId = chapter.id
        scriptureQueueRepo.syncActiveByChapter(abbr, chapter.id)

        if (book == null) return

        trackingRepo.recordReading(
            HistoryEntity(
                bibleAbbr = abbr,
                bibleName = state.value.activeBible,
                bookId = book.id,
                bookName = book.name,
                chapterId = chapter.id,
                chapterRef = chapter.reference,
                verseNumber = verses.firstOrNull()?.number ?: 1,
            )
        )

        val secondaryNames = parallelVerses.keys.mapNotNull { sAbbr ->
            state.value.savedBibles.find { it.abbreviation == sAbbr }?.name
        }
        val startIndex = resolvedTarget?.verseId
            ?.let { targetId -> verses.indexOfFirst { it.verseId == targetId } }
            ?.takeIf { it >= 0 } ?: 0

        castingRepo.publishReading(
            bibleName = state.value.activeBible,
            bookName = book.name,
            chapterRef = chapter.reference,
            verses = verses.map { it.text },
            indicators = verses.map { it.number.toString() },
            currentIndex = startIndex,
            multiBibleEnabled = multiBibleEnabled,
            secondaryBibleNames = secondaryNames,
        )
    }

    fun onVerseScrollPositionChanged(verseId: String, verseNumber: Int) {
        val chapter = state.value.activeChapter ?: return
        val book = state.value.activeBook ?: return
        val abbr = state.value.activeBibleAbbr
        prefsRepo.lastVerseId = verseId

        val verseIndex = state.value.verses.indexOfFirst { it.verseId == verseId }
        if (verseIndex >= 0) castingRepo.updateIndex(verseIndex)

        scope.launch {
            trackingRepo.recordReading(
                HistoryEntity(
                    bibleAbbr = abbr,
                    bibleName = state.value.activeBible,
                    bookId = book.id,
                    bookName = book.name,
                    chapterId = chapter.id,
                    chapterRef = chapter.reference,
                    verseNumber = verseNumber,
                )
            )
        }
    }

    fun consumeRestoreVerseTarget() {
        state.update { it.copy(restoreVerseId = null) }
    }
}
