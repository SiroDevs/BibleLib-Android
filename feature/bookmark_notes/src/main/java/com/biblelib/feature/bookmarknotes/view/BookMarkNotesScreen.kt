package com.biblelib.feature.bookmark_notes.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.action.ExpandScreenAction
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.core.ui.components.layout.CenteredContentFrame
import com.biblelib.feature.bookmark_notes.R
import com.biblelib.feature.bookmark_notes.viewmodel.BookmarkNotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
// Manages saved bookmarks and notes with bulk selection actions.
@Composable
fun BookmarkNotesScreen(
    navController: NavController,
    viewModel: BookmarkNotesViewModel,
    onExpand: (() -> Unit)? = null,
    isAdaptiveLayout: Boolean = false,
    onOpenInReader: ((String, String, String, String, String) -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val isSelectionMode = if (activeTab == 0) state.isBookmarkSelectionMode else state.isNoteSelectionMode
    val selectedCount = if (activeTab == 0) state.selectedBookmarkKeys.size else state.selectedNoteKeys.size

    fun openInReader(bibleAbbr: String, bookId: String, chapterId: String) {
        if (onOpenInReader != null) {
            onOpenInReader(bibleAbbr, bookId, chapterId, "", "")
        } else {
            navController.navigate(
                Routes.reader(bibleAbbr = bibleAbbr, bookId = bookId, chapterId = chapterId)
            ) {
                popUpTo(Routes.READER) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isSelectionMode) {
                    stringResource(R.string.bookmark_selected_count, selectedCount)
                } else {
                    stringResource(R.string.bookmark_notes_title)
                },
                showGoBack = true,
                closeOnBack = isAdaptiveLayout,
                onNavIconClick = {
                    if (isSelectionMode) {
                        if (activeTab == 0) viewModel.clearBookmarkSelection() else viewModel.clearNoteSelection()
                    } else {
                        navController.popBackStack()
                    }
                },
                actions = {
                    onExpand?.let { ExpandScreenAction(it) }
                    if (isSelectionMode) {
                        IconButton(onClick = { showDeleteSelectedDialog = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.bookmark_delete_selected))
                        }
                    } else if (isAdaptiveLayout) {
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                stringResource(
                                    if (activeTab == 0) R.string.bookmark_clear_all_bookmarks
                                    else R.string.bookmark_clear_all_notes,
                                ),
                            )
                        }
                    } else {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.bookmark_more))
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            if (activeTab == 0) R.string.bookmark_clear_all_bookmarks
                                            else R.string.bookmark_clear_all_notes,
                                        )
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                onClick = { showOverflowMenu = false; showClearAllDialog = true },
                            )
                        }
                    }
                },
            )
        }
    ) { padding ->
        CenteredContentFrame(
            modifier = Modifier.fillMaxSize().padding(padding),
            respectVerticalHinge = false,
        ) {
            Column(Modifier.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(stringResource(R.string.bookmark_tab_bookmarks)) },
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(stringResource(R.string.bookmark_tab_notes)) },
                )
            }

            when (activeTab) {
                0 -> BookmarksTab(
                    bookmarks = state.bookmarks,
                    isLoading = state.isLoading,
                    selectedKeys = state.selectedBookmarkKeys,
                    onTap = { item ->
                        if (state.isBookmarkSelectionMode) {
                            viewModel.toggleBookmarkSelection(item)
                        } else {
                            openInReader(item.bibleAbbr, item.bookId, item.chapterId)
                        }
                    },
                    onLongPress = viewModel::toggleBookmarkSelection,
                )
                1 -> NotesTab(
                    notes = state.notes,
                    isLoading = state.isLoading,
                    selectedKeys = state.selectedNoteKeys,
                    onTap = { item ->
                        if (state.isNoteSelectionMode) {
                            viewModel.toggleNoteSelection(item)
                        } else {
                            openInReader(item.bibleAbbr, item.bookId, item.chapterId)
                        }
                    },
                    onLongPress = viewModel::toggleNoteSelection,
                )
            }
            }
        }
    }

    if (showDeleteSelectedDialog) {
        ConfirmDialog(
            title = stringResource(R.string.bookmark_delete_selected_title),
            message = stringResource(R.string.bookmark_delete_selected_message, selectedCount),
            onConfirm = {
                if (activeTab == 0) viewModel.deleteSelectedBookmarks() else viewModel.deleteSelectedNotes()
                showDeleteSelectedDialog = false
            },
            onDismiss = { showDeleteSelectedDialog = false },
        )
    }

    if (showClearAllDialog) {
        ConfirmDialog(
            title = stringResource(
                if (activeTab == 0) R.string.bookmark_clear_bookmarks_title
                else R.string.bookmark_clear_notes_title,
            ),
            message = stringResource(
                R.string.bookmark_clear_all_message,
                stringResource(
                    if (activeTab == 0) R.string.bookmark_bookmarks_word
                    else R.string.bookmark_notes_word,
                ),
            ),
            onConfirm = {
                if (activeTab == 0) viewModel.clearAllBookmarks() else viewModel.clearAllNotes()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false },
        )
    }
}
