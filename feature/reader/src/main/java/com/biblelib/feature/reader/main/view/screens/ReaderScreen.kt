package com.biblelib.feature.reader.main.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.design_system.customization.AppFontFamilies
import com.biblelib.core.design_system.customization.AppReaderBackgrounds
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.core.ui.components.indicators.VerseShimmer
import com.biblelib.core.ui.components.share.ScreenshotReminderDialog
import com.biblelib.core.ui.components.share.ShareHelper
import com.biblelib.feature.reader.main.view.components.BibleSelectorSheet
import com.biblelib.feature.reader.main.view.components.BookDrawer
import com.biblelib.feature.reader.main.view.components.BookmarkOptionsDialog
import com.biblelib.feature.reader.main.view.components.ChapterSheet
import com.biblelib.feature.reader.main.view.components.HighlightColorPickerDialog
import com.biblelib.feature.reader.main.view.components.QuickSettingsDialog
import com.biblelib.feature.reader.main.view.components.ReaderBottomBar
import com.biblelib.feature.reader.main.view.components.ReaderFab
import com.biblelib.feature.reader.main.view.components.ReaderSelectionTopBar
import com.biblelib.feature.reader.main.view.components.ReaderTopBar
import com.biblelib.feature.reader.main.view.components.ScriptureQueue
import com.biblelib.feature.reader.main.view.components.VerseList
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    viewModel: ReaderViewModel,
    initialBible: String,
    initialBibleAbbr: String,
    initialBookId: String,
    initialChapterId: String,
    initialVerseId: String = "",
    initialSearchQry: String = "",
    themeRepo: ThemeRepo,
) {
    LaunchedEffect(Unit) {
        viewModel.initialize(
            initialBible,
            initialBibleAbbr,
            initialBookId,
            initialChapterId,
            initialVerseId,
            initialSearchQry,
        )
    }

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showBookDrawer by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var showBibleSelector by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val resolvedFontFamily = AppFontFamilies.byId(state.fontFamilyId).family
    val resolvedBackground = AppReaderBackgrounds.byId(state.readerBackgroundId)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeChapterIndex = state.chapters.indexOfFirst { it.id == state.activeChapter?.id }
    val hasPrevChapter = activeChapterIndex > 0
    val hasNextChapter = activeChapterIndex in 0 until state.chapters.size - 1
    val prevChapterLabel = state.chapters.getOrNull(activeChapterIndex - 1)?.reference ?: "Previous chapter"
    val nextChapterLabel = state.chapters.getOrNull(activeChapterIndex + 1)?.reference ?: "Next chapter"

    val itemIndexOffset = if (hasPrevChapter) 1 else 0

    LaunchedEffect(listState, state.verses, itemIndexOffset) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(500)
            .collect { index ->
                val verse = state.verses.getOrNull(index - itemIndexOffset) ?: return@collect
                viewModel.onVerseScrollPositionChanged(verse.verseId, verse.number)
            }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel = rememberUpdatedState(viewModel)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.value.refreshNotedVerses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.notesNavRequest) {
        val request = state.notesNavRequest ?: return@LaunchedEffect
        navController.navigate(
            Routes.notes(
                bibleAbbr = request.bibleAbbr,
                verseId = request.verseId,
                bookId = request.bookId,
                chapterId = request.chapterId,
                title = request.title,
                verseText = request.verseText,
            )
        )
        viewModel.consumeNotesNavRequest()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.isSelectionMode) {
                ReaderSelectionTopBar(
                    selectedCount = state.selectedVerseIds.size,
                    viewModel = viewModel,
                )
            } else {
                ReaderTopBar(
                    navController = navController,
                    state = state,
                    onBibleClick = { showBibleSelector = true },
                    onBookClick = { showBookDrawer = true },
                    bookSwitchEnabled = !state.isScriptureModeActive,
                    onBookSwitchBlocked = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "Finish or dismiss the scripture list to switch books"
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (state.isScriptureModeActive) {
                ScriptureQueue(
                    state = state,
                    viewModel = viewModel,
                    onQuickSettings = { showQuickSettings = true },
                )
            } else {
                ReaderBottomBar(
                    navController = navController,
                    viewModel = viewModel,
                    hasPrev = hasPrevChapter,
                    hasNext = hasNextChapter,
                    chapterRef = state.activeChapter?.number ?: "Chapter",
                    onChapterList = { showChapterSheet = true },
                    onQuickSettings = { showQuickSettings = true },
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(resolvedBackground.brush())
        ) {
            when {
                state.isLoading -> VerseShimmer()
                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = {
                        viewModel.initialize(
                            initialBible,
                            initialBibleAbbr,
                            initialBookId,
                            initialChapterId,
                            initialVerseId,
                            initialSearchQry,
                        )
                    }
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

    if (showBookDrawer) {
        BookDrawer(
            state = state,
            onSelect = { book ->
                viewModel.selectBook(book)
                showBookDrawer = false
            },
            onDismiss = { showBookDrawer = false },
        )
    }

    if (showChapterSheet) {
        ChapterSheet(
            state = state,
            onSelect = { ch ->
                viewModel.selectChapter(ch)
                showChapterSheet = false
            },
            onDismiss = { showChapterSheet = false },
        )
    }

    if (showBibleSelector) {
        BibleSelectorSheet(
            state = state,
            onSelect = { abbr ->
                viewModel.setPrimaryBible(abbr)
                showBibleSelector = false
            },
            onOpenBibles = {
                showBibleSelector = false
                navController.navigate(Routes.BIBLES)
            },
            onDismiss = { showBibleSelector = false },
        )
    }

    if (state.showColorPicker) {
        HighlightColorPickerDialog(
            colors = ReaderViewModel.HIGHLIGHT_COLORS,
            onColorChosen = viewModel::chooseHighlightColor,
            onDismiss = viewModel::dismissColorPicker,
        )
    }

    if (state.pendingHighlightColor != null) {
        BookmarkOptionsDialog(
            onBookmarkOnly = viewModel::confirmBookmarkOnly,
            onBookmarkWithNotes = { viewModel.confirmBookmarkWithNotes() },
            onDismiss = viewModel::cancelPendingHighlight,
        )
    }

    if (showQuickSettings) {
        QuickSettingsDialog(
            state = state,
            themeRepo = themeRepo,
            viewModel = viewModel,
            onDismiss = { showQuickSettings = false },
        )
    }

    if (!state.isLoading && state.error == null) {
        ScreenshotReminderDialog(
            onShareClick = {
                val text = if (state.isSelectionMode) {
                    viewModel.buildSelectionShareText()
                } else {
                    viewModel.buildActiveChapterShareText()
                }
                text?.let { ShareHelper.shareText(context, it) }
            },
        )
    }
}
