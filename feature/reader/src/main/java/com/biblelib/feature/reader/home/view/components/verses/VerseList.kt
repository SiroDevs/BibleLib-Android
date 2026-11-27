package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biblelib.feature.reader.home.utils.ChapterEdge
import com.biblelib.feature.reader.home.utils.NEXT_CHAPTER_KEY
import com.biblelib.feature.reader.home.utils.PREV_CHAPTER_KEY
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.view.components.others.ChapterEdgePrompt
import com.biblelib.feature.reader.home.view.components.others.ChapterTransition
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

// Displays chapter transitions and verses in a scrollable list.
@Composable
fun VerseList(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    fontFamily: FontFamily = FontFamily.Default,
    listState: LazyListState = rememberLazyListState(),
    hasPrevChapter: Boolean = false,
    hasNextChapter: Boolean = false,
    prevChapterLabel: String = "",
    nextChapterLabel: String = "",
    edgePrompt: ChapterEdgePrompt,
    sideBySideParallelText: Boolean = false,
    parallelTextGutter: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    val onEdgeReached: (ChapterEdge) -> Unit = { edge ->
        edgePrompt.show(edge, if (edge == ChapterEdge.START) prevChapterLabel else nextChapterLabel)
    }

    val hasParallel = state.parallelVerses.isNotEmpty()

    val itemIndexOffset = if (hasPrevChapter) 1 else 0

    LaunchedEffect(state.activeChapter?.id, state.verses, state.restoreVerseId) {
        val target = state.restoreVerseId
        if (target != null && state.verses.isNotEmpty()) {
            val idx = state.verses.indexOfFirst { it.verseId == target }
            if (idx >= 0) listState.scrollToItem(idx + itemIndexOffset)
            viewModel.consumeRestoreVerseTarget()
        }
    }

    ChapterEdgeEffect(
        edge = ChapterEdge.START,
        enabled = hasPrevChapter,
        listState = listState,
        chapterId = state.activeChapter?.id,
        onReached = onEdgeReached,
        onLeft = edgePrompt::hide,
    )
    ChapterEdgeEffect(
        edge = ChapterEdge.END,
        enabled = hasNextChapter,
        listState = listState,
        chapterId = state.activeChapter?.id,
        onReached = onEdgeReached,
        onLeft = edgePrompt::hide,
    )

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (hasPrevChapter) {
            item(key = PREV_CHAPTER_KEY) {
                ChapterTransition(
                    label = prevChapterLabel,
                    visible = ChapterEdge.START !in edgePrompt.hiddenEdges,
                    icon = Icons.Filled.KeyboardArrowUp,
                    iconAbove = true,
                )
            }
        }

        items(state.verses, key = { it.verseId }) { verse ->
            VerseRow(
                verse = verse,
                fontSizeSp = state.fontSizeSp,
                fontFamily = fontFamily,
                highlightQuery = state.highlightQuery,
                parallelTexts = if (hasParallel && !state.isSelectionMode) {
                    state.parallelVerses.mapValues { (_, pVerses) ->
                        pVerses.find { it.number == verse.number }?.text ?: ""
                    }
                } else emptyMap(),
                bookmarkColor = state.bookmarks[verse.verseId],
                isBookmarked = state.bookmarks.containsKey(verse.verseId),
                hasNote = verse.verseId in state.notedVerseIds,
                isSelected = verse.verseId in state.selectedVerseIds,
                isSelectionMode = state.isSelectionMode,
                sideBySideParallelText = sideBySideParallelText,
                parallelTextGutter = parallelTextGutter,
                onLongPress = { viewModel.toggleVerseSelected(verse.verseId) },
                onTap = { viewModel.toggleVerseSelected(verse.verseId) },
                onSwipeBookmark = { viewModel.quickToggleBookmark(verse.verseId) },
                onSwipeNotes = { viewModel.requestNotesForVerse(verse.verseId) },
            )
        }

        if (hasNextChapter) {
            item(key = NEXT_CHAPTER_KEY) {
                ChapterTransition(
                    label = nextChapterLabel,
                    visible = ChapterEdge.END !in edgePrompt.hiddenEdges,
                    icon = Icons.Filled.KeyboardArrowDown,
                    iconAbove = false,
                )
            }
        }
    }
}
