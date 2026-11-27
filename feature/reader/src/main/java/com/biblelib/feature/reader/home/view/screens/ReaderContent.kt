package com.biblelib.feature.reader.home.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biblelib.core.design_system.customization.AppFontFamilies
import com.biblelib.core.design_system.customization.AppReaderBackgrounds
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel
import androidx.navigation.NavController
import com.biblelib.feature.reader.home.view.components.actions.ReaderFab
import com.biblelib.feature.reader.home.view.components.others.ChapterEdgePrompt
import com.biblelib.feature.reader.home.view.components.actions.SpeedButtonsFab
import com.biblelib.feature.reader.home.view.components.verses.VerseList

// Renders the verse list within a readable width and reader controls.
@Composable
fun ReaderContent(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    navController: NavController,
    listState: LazyListState,
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
    prevChapterLabel: String,
    nextChapterLabel: String,
    isAutoScrolling: Boolean,
    edgePrompt: ChapterEdgePrompt,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    maxTextWidth: Dp = 760.dp,
    sideBySideParallelText: Boolean = false,
    parallelTextGutter: Dp = 24.dp,
) {
    val resolvedFontFamily = AppFontFamilies.byId(state.fontFamilyId).family
    val resolvedBackground = AppReaderBackgrounds.byId(state.readerBackgroundId)

    Box(
        modifier
            .fillMaxSize()
            .background(resolvedBackground.brush())
    ) {
        val textAreaModifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxHeight()
            .widthIn(max = maxTextWidth)
            .fillMaxWidth()

        when {
            state.isLoading -> Box(textAreaModifier) { VerseShimmer() }
            state.error != null -> Box(textAreaModifier) {
                ErrorState(message = state.error, onRetry = onRetry)
            }

            else -> VerseList(
                state = state,
                viewModel = viewModel,
                fontFamily = resolvedFontFamily,
                listState = listState,
                hasPrevChapter = hasPrevChapter,
                hasNextChapter = hasNextChapter,
                prevChapterLabel = prevChapterLabel,
                nextChapterLabel = nextChapterLabel,
                edgePrompt = edgePrompt,
                sideBySideParallelText = sideBySideParallelText,
                parallelTextGutter = parallelTextGutter,
                modifier = textAreaModifier,
            )
        }

        ReaderFab(
            state = state,
            modifier = Modifier.align(Alignment.BottomEnd),
            navController = navController,
            listState = listState,
        )

        SpeedButtonsFab(
            visible = isAutoScrolling,
            onSpeedUp = onSpeedUp,
            onSpeedDown = onSpeedDown,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}
