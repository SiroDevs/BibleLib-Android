package com.biblelib.feature.reader.home.view.components.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

@Composable
fun ReaderBottomBar(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    isAutoScrolling: Boolean,
    speedMultiplier: Float,
    onToggleAutoScroll: () -> Unit,
    onChapterList: () -> Unit,
    onQuickSettings: () -> Unit,
) {
    if (state.isScriptureModeActive) {
        ScriptureQueue(
            state = state,
            viewModel = viewModel,
            onQuickSettings = onQuickSettings,
        )
        return
    }

    val activeChapterIndex = state.chapters.indexOfFirst { it.id == state.activeChapter?.id }
    val hasPrev = activeChapterIndex > 0
    val hasNext = activeChapterIndex in 0 until state.chapters.size - 1
    val chapterRef = state.activeChapter?.number ?: "Chapter"

    NavigationBar(containerColor = MaterialTheme.colorScheme.onPrimary, tonalElevation = 4.dp) {
        NavigationBarItem(
            selected = isAutoScrolling,
            onClick = onToggleAutoScroll,
            icon = {
                Icon(
                    if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isAutoScrolling) "Stop auto scroll" else "Auto scroll",
                )
            },
            label = {
                Text(
                    if (isAutoScrolling) "Stop (${speedMultiplier}x)" else "Auto Scroll",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { viewModel.navigateChapter(-1) },
            enabled = hasPrev,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous") },
            label = { Text("Prev") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onChapterList,
            icon = { Icon(Icons.Default.MenuBook, "Chapters") },
            label = { Text("Chapter $chapterRef", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { viewModel.navigateChapter(1) },
            enabled = hasNext,
            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next") },
            label = { Text("Next") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onQuickSettings,
            icon = { Icon(Icons.Default.Tune, "Quick Settings") },
            label = { Text("Options") }
        )
    }
}
