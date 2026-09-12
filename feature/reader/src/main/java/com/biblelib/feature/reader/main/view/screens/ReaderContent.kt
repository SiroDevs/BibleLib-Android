package com.biblelib.feature.reader.main.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.biblelib.core.design_system.customization.AppFontFamilies
import com.biblelib.core.design_system.customization.AppReaderBackgrounds
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import androidx.navigation.NavController
import com.biblelib.feature.reader.main.view.components.actions.ReaderFab
import com.biblelib.feature.reader.main.view.components.verses.VerseList

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
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedFontFamily = AppFontFamilies.byId(state.fontFamilyId).family
    val resolvedBackground = AppReaderBackgrounds.byId(state.readerBackgroundId)

    Box(
        modifier
            .fillMaxSize()
            .background(resolvedBackground.brush())
    ) {
        when {
            state.isLoading -> VerseShimmer()
            state.error != null -> ErrorState(
                message = state.error,
                onRetry = onRetry,
            )

            else -> VerseList(
                state = state,
                viewModel = viewModel,
                fontFamily = resolvedFontFamily,
                listState = listState,
                hasPrevChapter = hasPrevChapter,
                hasNextChapter = hasNextChapter,
                prevChapterLabel = prevChapterLabel,
                nextChapterLabel = nextChapterLabel,
                onNavigatePrevChapter = { viewModel.navigateChapter(-1) },
                onNavigateNextChapter = { viewModel.navigateChapter(1) },
            )
        }

        ReaderFab(
            state = state,
            modifier = Modifier.align(Alignment.BottomEnd),
            navController = navController,
            listState = listState,
        )
    }
}
