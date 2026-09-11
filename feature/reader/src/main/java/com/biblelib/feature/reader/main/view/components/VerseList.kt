package com.biblelib.feature.reader.main.view.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.biblelib.feature.reader.main.utils.CHAPTER_TRANSITION_DELAY_MS
import com.biblelib.feature.reader.main.utils.NEXT_CHAPTER_KEY
import com.biblelib.feature.reader.main.utils.PREV_CHAPTER_KEY
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun VerseList(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    fontFamily: FontFamily = FontFamily.Default,
    listState: LazyListState = rememberLazyListState(),
    hasPrevChapter: Boolean = false,
    hasNextChapter: Boolean = false,
    prevChapterLabel: String = "Previous chapter",
    nextChapterLabel: String = "Next chapter",
    onNavigatePrevChapter: () -> Unit = {},
    onNavigateNextChapter: () -> Unit = {},
) {
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

    var isTransitioningPrev by remember(state.activeChapter?.id) { mutableStateOf(false) }
    var isTransitioningNext by remember(state.activeChapter?.id) { mutableStateOf(false) }

    LaunchedEffect(listState, hasPrevChapter, state.activeChapter?.id) {
        if (!hasPrevChapter) {
            isTransitioningPrev = false
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key == PREV_CHAPTER_KEY &&
                    !listState.canScrollBackward
        }.distinctUntilChanged().collectLatest { revealed ->
            if (revealed) {
                isTransitioningPrev = true
                delay(CHAPTER_TRANSITION_DELAY_MS)
                onNavigatePrevChapter()
            } else {
                isTransitioningPrev = false
            }
        }
    }

    LaunchedEffect(listState, hasNextChapter, state.activeChapter?.id) {
        if (!hasNextChapter) {
            isTransitioningNext = false
            return@LaunchedEffect
        }
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.key == NEXT_CHAPTER_KEY &&
                    !listState.canScrollForward
        }.distinctUntilChanged().collectLatest { revealed ->
            if (revealed) {
                isTransitioningNext = true
                delay(CHAPTER_TRANSITION_DELAY_MS)
                onNavigateNextChapter()
            } else {
                isTransitioningNext = false
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (hasPrevChapter) {
            item(key = PREV_CHAPTER_KEY) {
                ChapterTransitionIndicator(
                    label = prevChapterLabel,
                    icon = Icons.Filled.KeyboardArrowUp,
                    isTransitioning = isTransitioningPrev,
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
                onLongPress = { viewModel.toggleVerseSelected(verse.verseId) },
                onTap = { viewModel.toggleVerseSelected(verse.verseId) },
                onSwipeBookmark = { viewModel.quickToggleBookmark(verse.verseId) },
                onSwipeNotes = { viewModel.requestNotesForVerse(verse.verseId) },
            )
        }

        if (hasNextChapter) {
            item(key = NEXT_CHAPTER_KEY) {
                ChapterTransitionIndicator(
                    label = nextChapterLabel,
                    icon = Icons.Filled.KeyboardArrowDown,
                    isTransitioning = isTransitioningNext,
                    iconAbove = false,
                )
            }
        }
    }
}
