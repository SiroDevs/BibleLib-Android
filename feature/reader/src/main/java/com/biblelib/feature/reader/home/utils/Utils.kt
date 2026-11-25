package com.biblelib.feature.reader.home.utils

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.core.database.entities.ScriptureItemEntity

const val SWIPE_ACTION_TRIGGER_PX = 140f

// Keys for the sentinel items shown at the edges of a chapter, see [VerseList]
const val PREV_CHAPTER_KEY = "__prev_chapter_sentinel__"
const val NEXT_CHAPTER_KEY = "__next_chapter_sentinel__"

const val CHAPTER_EDGE_PROMPT_TIMEOUT_MS = 5_000L

// Chapter edge the reader has scrolled to; direction is the chapter offset to navigate by
enum class ChapterEdge(val direction: Int) {
    START(-1),
    END(1),
}

data class NotesNavRequest(
    val bibleAbbr: String,
    val verseId: String,
    val bookId: String,
    val chapterId: String,
    val title: String,
    val verseText: String,
)

data class ScrollTarget(
    val verseId: String,
    val highlightQuery: String? = null,
)

data class ReaderUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val savedBibles: List<BibleEntity> = emptyList(),
    val activeBible: String = "",
    val activeBibleAbbr: String = "",
    val books: List<BookEntity> = emptyList(),
    val activeBook: BookEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val activeChapter: ChapterEntity? = null,
    val verses: List<VerseDisplay> = emptyList(),
    val parallelVerses: Map<String, List<VerseDisplay>> = emptyMap(),
    val fontSizeSp: Float = 18f,
    val fontFamilyId: String = "default",
    val readerBackgroundId: String = "default",
    val multiBibleReaderEnabled: Boolean = true,
    val restoreVerseId: String? = null,
    val highlightQuery: String? = null,

    val bookmarks: Map<String, String?> = emptyMap(),
    val notedVerseIds: Set<String> = emptySet(),

    val selectedVerseIds: Set<String> = emptySet(),
    val showColorPicker: Boolean = false,
    val pendingHighlightColor: String? = null,
    val notesNavRequest: NotesNavRequest? = null,

    val downloadProgress: Map<String, Float> = emptyMap(),

    val queueItems: List<ScriptureItemEntity> = emptyList(),
    val queueActiveItemId: Long? = null,
) {
    val isSelectionMode: Boolean get() = selectedVerseIds.isNotEmpty()
    val isScriptureModeActive: Boolean get() = queueItems.size > 1
    val activeBibleLanguage: String?
        get() = savedBibles.find { it.abbreviation == activeBibleAbbr }?.languageName
}