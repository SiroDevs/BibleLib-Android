package com.biblelib.feature.reader.home.view.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.components.review.ReviewPromptHost
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.feature.reader.R
import com.biblelib.feature.reader.home.view.components.others.ChurchLibPromo
import com.biblelib.feature.reader.home.view.components.others.rememberChapterEdgePrompt
import com.biblelib.feature.reader.home.view.components.others.ReaderOverlays
import com.biblelib.feature.reader.home.view.components.others.rememberAutoScrollController
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// Owns reader state, lifecycle updates, and responsive screen composition.
@OptIn(FlowPreview::class)
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
    prefsRepo: PrefsRepo,
    adaptiveWindowInfo: AdaptiveWindowInfo,
    initializeOnEntry: Boolean = true,
) {
    LaunchedEffect(Unit) {
        if (initializeOnEntry) {
            viewModel.initialize(
                initialBible,
                initialBibleAbbr,
                initialBookId,
                initialChapterId,
                initialVerseId,
                initialSearchQry,
            )
        }
    }

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showBookDrawer by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var showBibleSelector by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showWideBookPane by rememberSaveable { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val autoScroll = rememberAutoScrollController(listState)

    val activeChapterIndex = state.chapters.indexOfFirst { it.id == state.activeChapter?.id }
    val hasPrevChapter = activeChapterIndex > 0
    val hasNextChapter = activeChapterIndex in 0 until state.chapters.size - 1
    val prevChapterLabel = state.chapters.getOrNull(activeChapterIndex - 1)?.reference.orEmpty()
    val nextChapterLabel = state.chapters.getOrNull(activeChapterIndex + 1)?.reference.orEmpty()
    val chapterEdgePrompt = rememberChapterEdgePrompt(
        hostState = snackbarHostState,
        chapterId = state.activeChapter?.id,
        onAccept = viewModel::navigateChapter,
    )
    val itemIndexOffset = if (hasPrevChapter) 1 else 0
    val canShowPersistentBookPane =
        adaptiveWindowInfo.widthClass >= AdaptiveWidthClass.LARGE &&
                !adaptiveWindowInfo.hasVerticalSeparatingHinge &&
                !state.isSelectionMode && !state.isScriptureModeActive

    LaunchedEffect(listState, state.verses, itemIndexOffset) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(500)
            .collect { index ->
                val verse = state.verses.getOrNull(index - itemIndexOffset) ?: return@collect
                viewModel.onVerseScrollPositionChanged(verse.verseId, verse.number)
            }
    }

    var churchLibPromoVisible by remember { mutableStateOf(false) }
    val promptsAllowed = !autoScroll.isAutoScrolling &&
            !showBookDrawer && !showChapterSheet &&
            !showBibleSelector && !showQuickSettings

    ChurchLibPromo(
        prefsRepo = prefsRepo,
        enabled = promptsAllowed,
        onVisibleChange = { churchLibPromoVisible = it },
    )
    ReviewPromptHost(enabled = promptsAllowed && !churchLibPromoVisible)

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

    AdaptiveReaderLayout(
        navController = navController,
        viewModel = viewModel,
        state = state,
        adaptiveWindowInfo = adaptiveWindowInfo,
        listState = listState,
        snackbarHostState = snackbarHostState,
        autoScroll = autoScroll,
        edgePrompt = chapterEdgePrompt,
        hasPrevChapter = hasPrevChapter,
        hasNextChapter = hasNextChapter,
        prevChapterLabel = prevChapterLabel,
        nextChapterLabel = nextChapterLabel,
        showPersistentBookPane = canShowPersistentBookPane && showWideBookPane,
        onBibleClick = { showBibleSelector = true },
        onBookClick = {
            if (canShowPersistentBookPane) {
                showWideBookPane = !showWideBookPane
            } else {
                showBookDrawer = true
            }
        },
        onBookSwitchBlocked = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.scripture_mode_switch_blocked)
                )
            }
        },
        onChapterList = {
            if (canShowPersistentBookPane) {
                showWideBookPane = true
            } else {
                showChapterSheet = true
            }
        },
        onQuickSettings = { showQuickSettings = true },
        onRetry = {
            viewModel.initialize(
                initialBible,
                initialBibleAbbr,
                initialBookId,
                initialChapterId,
                initialVerseId,
                initialSearchQry,
            )
        },
    )

    ReaderOverlays(
        state = state,
        viewModel = viewModel,
        navController = navController,
        themeRepo = themeRepo,
        context = context,
        showBookDrawer = showBookDrawer,
        onDismissBookDrawer = { showBookDrawer = false },
        showChapterSheet = showChapterSheet,
        onDismissChapterSheet = { showChapterSheet = false },
        showBibleSelector = showBibleSelector,
        onDismissBibleSelector = { showBibleSelector = false },
        showQuickSettings = showQuickSettings,
        onDismissQuickSettings = { showQuickSettings = false },
    )
}
