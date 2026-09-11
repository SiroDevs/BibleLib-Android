package com.biblelib.feature.reader.main.viewmodel.controller

import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.core.data.repos.AnnotationRepo
import com.biblelib.feature.reader.main.utils.NotesNavRequest
import com.biblelib.feature.reader.main.utils.ReaderUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnnotationController(
    private val annotationRepo: AnnotationRepo,
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<ReaderUiState>,
) {
    fun toggleVerseSelected(verseId: String) {
        state.update {
            val newSelection = if (verseId in it.selectedVerseIds) {
                it.selectedVerseIds - verseId
            } else {
                it.selectedVerseIds + verseId
            }
            it.copy(selectedVerseIds = newSelection)
        }
    }

    fun clearSelection() {
        state.update {
            it.copy(
                selectedVerseIds = emptySet(),
                showColorPicker = false,
                pendingHighlightColor = null,
            )
        }
    }

    fun quickToggleBookmark(verseId: String) {
        val current = state.value
        val abbr = current.activeBibleAbbr
        val bookId = current.activeBook?.id ?: return
        val chapterId = current.activeChapter?.id ?: return

        scope.launch {
            if (verseId in current.bookmarks) {
                annotationRepo.removeBookmarks(abbr, listOf(verseId))
                state.update { it.copy(bookmarks = it.bookmarks - verseId) }
            } else {
                annotationRepo.setBookmarks(abbr, listOf(verseId), bookId, chapterId, colorHex = null)
                state.update { it.copy(bookmarks = it.bookmarks + (verseId to null)) }
            }
        }
    }

    fun requestNotesForVerse(verseId: String): NotesNavRequest? {
        val current = state.value
        val verse = current.verses.find { it.verseId == verseId } ?: return null
        val request = buildNotesNavRequest(verse)
        state.update { it.copy(notesNavRequest = request) }
        return request
    }

    fun consumeNotesNavRequest() {
        state.update { it.copy(notesNavRequest = null) }
    }

    fun openColorPicker() {
        state.update { it.copy(showColorPicker = true) }
    }

    fun dismissColorPicker() {
        state.update { it.copy(showColorPicker = false) }
    }

    fun chooseHighlightColor(colorHex: String) {
        state.update { it.copy(pendingHighlightColor = colorHex, showColorPicker = false) }
    }

    fun cancelPendingHighlight() {
        state.update { it.copy(pendingHighlightColor = null) }
    }

    fun openNotesForSelection(): NotesNavRequest? {
        val current = state.value
        val verseId = current.selectedVerseIds.singleOrNull() ?: return null
        val verse = current.verses.find { it.verseId == verseId } ?: return null
        val request = buildNotesNavRequest(verse)
        state.update { it.copy(notesNavRequest = request, selectedVerseIds = emptySet()) }
        return request
    }

    fun confirmBookmarkOnly() {
        val current = state.value
        val color = current.pendingHighlightColor ?: return
        val verseIds = current.selectedVerseIds
        val bookId = current.activeBook?.id ?: return
        val chapterId = current.activeChapter?.id ?: return

        scope.launch {
            annotationRepo.setBookmarks(current.activeBibleAbbr, verseIds, bookId, chapterId, color)
            state.update {
                val updated = it.bookmarks.toMutableMap().apply {
                    verseIds.forEach { id -> put(id, color) }
                }
                it.copy(
                    bookmarks = updated,
                    selectedVerseIds = emptySet(),
                    pendingHighlightColor = null,
                )
            }
        }
    }

    fun confirmBookmarkWithNotes(): NotesNavRequest? {
        val current = state.value
        val color = current.pendingHighlightColor ?: return null
        val verseIds = current.selectedVerseIds
        val bookId = current.activeBook?.id ?: return null
        val chapterId = current.activeChapter?.id ?: return null
        val firstVerseId = verseIds.firstOrNull() ?: return null
        val verse = current.verses.find { it.verseId == firstVerseId } ?: return null

        val request = buildNotesNavRequest(verse)

        scope.launch {
            annotationRepo.setBookmarks(current.activeBibleAbbr, verseIds, bookId, chapterId, color)
        }

        state.update {
            val updated = it.bookmarks.toMutableMap().apply {
                verseIds.forEach { id -> put(id, color) }
            }
            it.copy(
                bookmarks = updated,
                selectedVerseIds = emptySet(),
                pendingHighlightColor = null,
                notesNavRequest = request,
            )
        }
        return request
    }

    fun refreshNotedVerses() {
        val abbr = state.value.activeBibleAbbr
        val chapterId = state.value.activeChapter?.id ?: return
        scope.launch {
            val noted = annotationRepo.getNotedVerseIds(abbr, chapterId)
            state.update { it.copy(notedVerseIds = noted) }
        }
    }

    /** Formatted text for the currently selected verse(s) — used by both Share and Copy in
     *  [com.biblelib.feature.reader.main.view.components.ReaderSelectionTopBar]. */
    fun buildSelectionShareText(): String? {
        val current = state.value
        if (current.selectedVerseIds.isEmpty()) return null
        val selected = current.verses
            .filter { it.verseId in current.selectedVerseIds }
            .sortedBy { it.number }
        if (selected.isEmpty()) return null

        val reference = "${referencePrefix(current)}${formatVerseRange(selected.map { it.number })}"
        val body = selected.joinToString("\n") { "${it.number} ${it.text}" }
        return buildShareText(reference, body, current)
    }

    /** Formatted text for the whole active chapter — the fallback used when a screenshot is
     *  detected while no verse is currently selected. */
    fun buildActiveChapterShareText(): String? {
        val current = state.value
        if (current.verses.isEmpty()) return null

        val reference = referencePrefix(current).trimEnd(':', ' ')
        val body = current.verses
            .sortedBy { it.number }
            .joinToString("\n") { "${it.number} ${it.text}" }
        return buildShareText(reference, body, current)
    }

    private fun referencePrefix(current: ReaderUiState): String {
        val bookName = current.activeBook?.name ?: current.verses.firstOrNull()?.bookId ?: ""
        val chapterNumber = current.activeChapter?.number
        return if (chapterNumber.isNullOrBlank()) "$bookName " else "$bookName $chapterNumber:"
    }

    private fun buildShareText(reference: String, body: String, current: ReaderUiState): String {
        val footnote = buildString {
            append(current.activeBible)
            val language = current.activeBibleLanguage
            if (!language.isNullOrBlank()) append(" ($language)")
        }
        return buildString {
            append(reference)
            append("\n\n")
            append(body)
            append("\n\n— ")
            append(footnote)
        }
    }

    /** Collapses a list of verse numbers into a compact range string, e.g. [16,17,18,20]
     *  becomes "16-18, 20". */
    private fun formatVerseRange(numbers: List<Int>): String {
        if (numbers.isEmpty()) return ""
        val sorted = numbers.sorted()
        val parts = mutableListOf<String>()
        var start = sorted.first()
        var prev = sorted.first()
        for (n in sorted.drop(1)) {
            if (n == prev + 1) {
                prev = n
                continue
            }
            parts += if (start == prev) "$start" else "$start-$prev"
            start = n
            prev = n
        }
        parts += if (start == prev) "$start" else "$start-$prev"
        return parts.joinToString(", ")
    }

    private fun buildNotesNavRequest(verse: VerseDisplay): NotesNavRequest {
        val current = state.value
        val bookName = current.activeBook?.name ?: verse.bookId
        val chapterNumber = current.activeChapter?.number ?: ""
        val title = "$bookName $chapterNumber:${verse.number}"
        return NotesNavRequest(
            bibleAbbr = current.activeBibleAbbr,
            verseId = verse.verseId,
            bookId = current.activeBook?.id ?: verse.bookId,
            chapterId = current.activeChapter?.id ?: verse.chapterId,
            title = title,
            verseText = verse.text,
        )
    }

    companion object {
        val HIGHLIGHT_COLORS = listOf(
            "#FFF59D", // yellow
            "#A5D6A7", // green
            "#90CAF9", // blue
            "#F48FB1", // pink
            "#FFCC80", // orange
            "#CE93D8", // purple
        )
    }
}