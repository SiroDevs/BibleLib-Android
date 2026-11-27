package com.biblelib.feature.reader.home.view.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavController
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.view.components.actions.ReaderBottomBar
import com.biblelib.feature.reader.home.view.components.actions.ReaderTopBar
import com.biblelib.feature.reader.home.view.components.others.AutoScrollController
import com.biblelib.feature.reader.home.view.components.others.ChapterEdgePrompt
import com.biblelib.feature.reader.home.view.components.others.ReaderSnackbarHost
import com.biblelib.feature.reader.home.view.components.sheets.BookNavigationPane
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
// Reflows reader controls and panes around window size and fold posture.
@Composable
fun AdaptiveReaderLayout(
    navController: NavController,
    viewModel: ReaderViewModel,
    state: ReaderUiState,
    adaptiveWindowInfo: AdaptiveWindowInfo,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    autoScroll: AutoScrollController,
    edgePrompt: ChapterEdgePrompt,
    hasPrevChapter: Boolean,
    hasNextChapter: Boolean,
    prevChapterLabel: String,
    nextChapterLabel: String,
    showPersistentBookPane: Boolean,
    onBibleClick: () -> Unit,
    onBookClick: () -> Unit,
    onBookSwitchBlocked: () -> Unit,
    onChapterList: () -> Unit,
    onQuickSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    val bookPosture = adaptiveWindowInfo.widthClass >= AdaptiveWidthClass.MEDIUM &&
            adaptiveWindowInfo.hasVerticalSeparatingHinge
    val sideBySideParallelText = state.parallelVerses.isNotEmpty() &&
            (adaptiveWindowInfo.widthClass >= AdaptiveWidthClass.LARGE || bookPosture) &&
            !state.isSelectionMode && !state.isScriptureModeActive
    val showBookPosturePane = bookPosture && !sideBySideParallelText &&
            !state.isSelectionMode && !state.isScriptureModeActive
    val maxReaderWidth = if (sideBySideParallelText) 1200.dp else 760.dp
    val hingeGutter = adaptiveWindowInfo.verticalHingeGutter +
            if (bookPosture) 48.dp else 0.dp
    val foldGutter = hingeGutter.takeIf { bookPosture }

    Scaffold(
        snackbarHost = { ReaderSnackbarHost(snackbarHostState) },
        topBar = {
            ReaderTopBar(
                navController = navController,
                state = state,
                viewModel = viewModel,
                onBibleClick = onBibleClick,
                onBookClick = onBookClick,
                bookSwitchEnabled = !state.isScriptureModeActive,
                onBookSwitchBlocked = onBookSwitchBlocked,
            )
        },
        bottomBar = {
            ReaderBottomBar(
                state = state,
                viewModel = viewModel,
                isAutoScrolling = autoScroll.isAutoScrolling,
                speedMultiplier = autoScroll.speedMultiplier,
                onToggleAutoScroll = autoScroll::toggle,
                onChapterList = onChapterList,
                onQuickSettings = onQuickSettings,
                hingeGutter = foldGutter,
            )
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding).fillMaxSize()
        when {
            adaptiveWindowInfo.isTabletop -> TabletopReaderContent(
                modifier = contentModifier,
                horizontalHingeClearance = adaptiveWindowInfo.horizontalHingeClearance,
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
                parallelTextGutter = hingeGutter,
            )

            showBookPosturePane -> Row(contentModifier) {
                ReaderPane(
                    modifier = Modifier.weight(1f),
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
                    parallelTextGutter = adaptiveWindowInfo.verticalHingeGutter,
                )
                Spacer(Modifier.width(hingeGutter))
                BookNavigationPane(
                    state = state,
                    onSelectBook = viewModel::selectBook,
                    onSelectChapter = viewModel::selectChapter,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            bookPosture && !sideBySideParallelText -> Row(contentModifier) {
                ReaderPane(
                    modifier = Modifier.weight(1f),
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
                    parallelTextGutter = hingeGutter,
                )
                Spacer(Modifier.width(hingeGutter))
                Spacer(Modifier.weight(1f))
            }

            showPersistentBookPane -> Row(contentModifier) {
                BookNavigationPane(
                    state = state,
                    onSelectBook = viewModel::selectBook,
                    onSelectChapter = viewModel::selectChapter,
                    modifier = Modifier.width(280.dp).fillMaxHeight(),
                )
                ReaderPane(
                    modifier = Modifier.weight(1f),
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
                    parallelTextGutter = hingeGutter,
                )
            }

            else -> ReaderPane(
                modifier = contentModifier,
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
                parallelTextGutter = hingeGutter,
            )
        }
    }
}
