package com.biblelib.feature.reader.home.view.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavController
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.view.components.others.AutoScrollController
import com.biblelib.feature.reader.home.view.components.others.ChapterEdgePrompt
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

// Keeps reading content in the upper half of a tabletop foldable.
@Composable
internal fun TabletopReaderContent(
    modifier: Modifier,
    horizontalHingeClearance: Dp,
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    navController: NavController,
    listState: LazyListState,
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
    prevChapterLabel: String,
    nextChapterLabel: String,
    autoScroll: AutoScrollController,
    edgePrompt: ChapterEdgePrompt,
    onRetry: () -> Unit,
    maxReaderWidth: Dp,
    sideBySideParallelText: Boolean,
    parallelTextGutter: Dp,
) {
    BoxWithConstraints(modifier) {
        val safeContentHeight = ((maxHeight - horizontalHingeClearance) / 2).coerceAtLeast(0.dp)
        ReaderPane(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(safeContentHeight),
            state = state,
            viewModel = viewModel,
            navController = navController,
            listState = listState,
            hasPrevChapter = hasPrevChapter,
            hasNextChapter = hasNextChapter,
            prevChapterLabel = prevChapterLabel,
            nextChapterLabel = nextChapterLabel,
            autoScroll = autoScroll,
            edgePrompt = edgePrompt,
            onRetry = onRetry,
            maxReaderWidth = maxReaderWidth,
            sideBySideParallelText = sideBySideParallelText,
            parallelTextGutter = parallelTextGutter,
        )
    }
}

// Groups the reader, chapter edge controls, and verse content.
@Composable
internal fun ReaderPane(
    modifier: Modifier,
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    navController: NavController,
    listState: LazyListState,
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
    prevChapterLabel: String,
    nextChapterLabel: String,
    autoScroll: AutoScrollController,
    edgePrompt: ChapterEdgePrompt,
    onRetry: () -> Unit,
    maxReaderWidth: Dp,
    sideBySideParallelText: Boolean = false,
    parallelTextGutter: Dp = 24.dp,
) {
    ReaderContent(
        modifier = modifier,
        state = state,
        viewModel = viewModel,
        navController = navController,
        listState = listState,
        hasPrevChapter = hasPrevChapter,
        hasNextChapter = hasNextChapter,
        prevChapterLabel = prevChapterLabel,
        nextChapterLabel = nextChapterLabel,
        isAutoScrolling = autoScroll.isAutoScrolling,
        edgePrompt = edgePrompt,
        onSpeedUp = autoScroll::speedUp,
        onSpeedDown = autoScroll::speedDown,
        onRetry = onRetry,
        maxTextWidth = maxReaderWidth,
        sideBySideParallelText = sideBySideParallelText,
        parallelTextGutter = parallelTextGutter,
    )
}
