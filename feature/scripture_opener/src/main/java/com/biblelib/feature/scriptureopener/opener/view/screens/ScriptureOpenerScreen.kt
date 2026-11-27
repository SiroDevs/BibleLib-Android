package com.biblelib.feature.scripture_opener.opener.view.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.action.ExpandScreenAction
import com.biblelib.feature.scripture_opener.opener.view.components.ScriptureSearch
import com.biblelib.feature.scripture_opener.opener.viewmodel.ScriptureOpenerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureOpenerScreen(
    navController: NavHostController,
    viewModel: ScriptureOpenerViewModel,
    bibleAbbr: String,
    bibleName: String,
    onExpand: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(bibleAbbr, bibleName) {
        viewModel.initialize(bibleAbbr, bibleName)
    }

    LaunchedEffect(state.navigateToReader) {
        val target = state.navigateToReader ?: return@LaunchedEffect
        navController.navigate(Routes.reader(target.bibleAbbr, target.bookId, target.chapterId)) {
            popUpTo(Routes.SCRIPTURE_OPENER) { inclusive = true }
        }
        viewModel.consumeNavigation()
    }

    LaunchedEffect(state.closeRequested) {
        if (state.closeRequested) {
            navController.popBackStack()
            viewModel.consumeClose()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Scripture Opener",
                tagline = bibleName,
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = { onExpand?.let { ExpandScreenAction(it) } },
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.error ?: "Something went wrong.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }

            else -> {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(state.rows, key = { it.key }) { row ->
                        ScriptureSearch(
                            row = row,
                            isActive = row.key == state.activeRow?.key,
                            onToggleField = { field -> viewModel.toggleField(row.key, field) },
                            onSelectBook = { book -> viewModel.selectBook(row.key, book) },
                            onSelectChapter = { chapter -> viewModel.selectChapter(row.key, chapter) },
                            onSelectVerse = { number -> viewModel.selectVerse(row.key, number) },
                            onOpenScripture = { viewModel.openScripture(row.key) },
                            onAddToQueue = { viewModel.addToQueue(row.key) },
                            onAddToQueueAndClose = { viewModel.addToQueueAndClose(row.key) },
                            onAddToQueueAndFinish = { viewModel.addToQueueAndFinish(row.key) },
                        )
                    }
                }
                LaunchedEffect(state.rows.size) {
                    if (state.rows.isNotEmpty()) listState.animateScrollToItem(state.rows.lastIndex)
                }
            }
        }
    }
}
