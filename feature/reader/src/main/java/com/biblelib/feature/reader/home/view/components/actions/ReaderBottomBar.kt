package com.biblelib.feature.reader.home.view.components.actions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biblelib.feature.reader.R
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

@Composable
// Shows reading actions with a clear gap over a separating vertical hinge.
fun ReaderBottomBar(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    isAutoScrolling: Boolean,
    speedMultiplier: Float,
    onToggleAutoScroll: () -> Unit,
    onChapterList: () -> Unit,
    onQuickSettings: () -> Unit,
    hingeGutter: Dp? = null,
) {
    if (state.isScriptureModeActive) {
        ScriptureQueue(
            state = state,
            viewModel = viewModel,
            onQuickSettings = onQuickSettings,
            hingeGutter = hingeGutter,
        )
        return
    }

    val activeChapterIndex = state.chapters.indexOfFirst { it.id == state.activeChapter?.id }
    val hasPrev = activeChapterIndex > 0
    val hasNext = activeChapterIndex in 0 until state.chapters.size - 1
    val chapterRef = state.activeChapter?.number
        ?: stringResource(R.string.reader_chapter_fallback)

    if (hingeGutter != null) {
        Row {
            NavigationBar(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 0.dp,
            ) {
                AutoScrollItem(isAutoScrolling, speedMultiplier, onToggleAutoScroll)
                PreviousChapterItem(hasPrev) { viewModel.navigateChapter(-1) }
            }
            Spacer(Modifier.width(hingeGutter))
            NavigationBar(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 0.dp,
            ) {
                ChapterItem(chapterRef, onChapterList)
                NextChapterItem(hasNext) { viewModel.navigateChapter(1) }
                QuickSettingsItem(onQuickSettings)
            }
        }
    } else {
        NavigationBar(containerColor = MaterialTheme.colorScheme.onPrimary, tonalElevation = 4.dp) {
            AutoScrollItem(isAutoScrolling, speedMultiplier, onToggleAutoScroll)
            PreviousChapterItem(hasPrev) { viewModel.navigateChapter(-1) }
            ChapterItem(chapterRef, onChapterList)
            NextChapterItem(hasNext) { viewModel.navigateChapter(1) }
            QuickSettingsItem(onQuickSettings)
        }
    }
}

@Composable
// Provides automatic scrolling controls within a navigation bar row.
private fun RowScope.AutoScrollItem(
    isAutoScrolling: Boolean,
    speedMultiplier: Float,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = isAutoScrolling,
        onClick = onClick,
        icon = {
            Icon(
                if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isAutoScrolling) R.string.reader_stop_auto_scroll_content_description
                    else R.string.reader_auto_scroll_content_description,
                ),
            )
        },
        label = {
            Text(
                if (isAutoScrolling) stringResource(
                    R.string.reader_stop_auto_scroll_label,
                    speedMultiplier.toString(),
                ) else stringResource(R.string.reader_auto_scroll_label),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
// Navigates to the preceding chapter.
private fun RowScope.PreviousChapterItem(enabled: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        enabled = enabled,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(R.string.reader_previous_chapter_content_description),
            )
        },
        label = { Text(stringResource(R.string.reader_previous_chapter_label)) },
    )
}

@Composable
// Opens the chapter picker from the navigation bar.
private fun RowScope.ChapterItem(chapterRef: String, onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                stringResource(R.string.reader_chapters_content_description),
            )
        },
        label = {
            Text(
                stringResource(R.string.reader_chapter_label, chapterRef),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
// Navigates to the following chapter.
private fun RowScope.NextChapterItem(enabled: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        enabled = enabled,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                stringResource(R.string.reader_next_chapter_content_description),
            )
        },
        label = { Text(stringResource(R.string.reader_next_chapter_label)) },
    )
}

@Composable
// Opens reader display options.
private fun RowScope.QuickSettingsItem(onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                Icons.Default.Tune,
                stringResource(R.string.reader_quick_settings_content_description),
            )
        },
        label = { Text(stringResource(R.string.reader_options_label)) },
    )
}
