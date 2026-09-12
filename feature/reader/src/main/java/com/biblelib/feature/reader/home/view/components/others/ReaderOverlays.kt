package com.biblelib.feature.reader.home.view.components.others

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.components.share.ScreenshotReminderDialog
import com.biblelib.core.ui.components.share.ShareHelper
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.view.components.sheets.BibleSelectorSheet
import com.biblelib.feature.reader.home.view.components.sheets.BookDrawer
import com.biblelib.feature.reader.home.view.components.sheets.ChapterSheet
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

@Composable
fun ReaderOverlays(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    navController: NavController,
    themeRepo: ThemeRepo,
    context: Context,
    showBookDrawer: Boolean,
    onDismissBookDrawer: () -> Unit,
    showChapterSheet: Boolean,
    onDismissChapterSheet: () -> Unit,
    showBibleSelector: Boolean,
    onDismissBibleSelector: () -> Unit,
    showQuickSettings: Boolean,
    onDismissQuickSettings: () -> Unit,
) {
    if (showBookDrawer) {
        BookDrawer(
            state = state,
            onSelect = { book ->
                viewModel.selectBook(book)
                onDismissBookDrawer()
            },
            onDismiss = onDismissBookDrawer,
        )
    }

    if (showChapterSheet) {
        ChapterSheet(
            state = state,
            onSelect = { ch ->
                viewModel.selectChapter(ch)
                onDismissChapterSheet()
            },
            onDismiss = onDismissChapterSheet,
        )
    }

    if (showBibleSelector) {
        BibleSelectorSheet(
            state = state,
            onSelect = { abbr ->
                viewModel.setPrimaryBible(abbr)
                onDismissBibleSelector()
            },
            onOpenBibles = {
                onDismissBibleSelector()
                navController.navigate(Routes.BIBLES)
            },
            onDismiss = onDismissBibleSelector,
        )
    }

    if (state.showColorPicker) {
        HighlightColorPickerDialog(viewModel = viewModel)
    }

    if (state.pendingHighlightColor != null) {
        BookmarkOptionsDialog(viewModel = viewModel)
    }

    if (showQuickSettings) {
        QuickSettingsDialog(
            state = state,
            themeRepo = themeRepo,
            viewModel = viewModel,
            onDismiss = onDismissQuickSettings,
        )
    }

    if (!state.isLoading && state.error == null) {
        ScreenshotReminderDialog(
            onShareClick = {
                viewModel.buildShareText()?.let { ShareHelper.shareText(context, it) }
            },
        )
    }
}
