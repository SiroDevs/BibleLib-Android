package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel

@Composable
fun ReaderBottomBar(
    navController: NavController,
    state: ReaderUiState,
    viewModel: ReaderViewModel,
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
            selected = false,
            onClick = { navController.navigate(Routes.SCRIPTURE_LISTS) },
            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, "Scriptures") },
            label = { Text("Scriptures") }
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

@Composable
private fun ScriptureQueue(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    onQuickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                items(state.queueItems, key = { it.id }) { item ->
                    val isActive = item.id == state.queueActiveItemId
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { viewModel.jumpToQueueItem(item) },
                    ) {
                        Text(
                            text = "${item.bookAbbr.uppercase()} ${item.chapterNumber}:${item.verseNumber}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            IconButton(onClick = onQuickSettings) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Quick Settings",
                )
            }
            IconButton(onClick = viewModel::dismissScriptureQueue) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close scripture list",
                )
            }
        }
    }
}
