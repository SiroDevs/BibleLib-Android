package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.biblelib.core.common.entity.VerseDisplay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val USER_SCROLL_SETTLE_MS = 750L

// Reports the visible verse after user scrolling or auto scrolling settles.
@OptIn(FlowPreview::class)
@Composable
fun TrackVerseScrollPosition(
    listState: LazyListState,
    verses: List<VerseDisplay>,
    itemIndexOffset: Int,
    isAutoScrolling: Boolean,
    onPositionChanged: (String, Int) -> Unit,
) {
    var isUserScrollActive by remember { mutableStateOf(false) }
    val currentUserScrollActive = rememberUpdatedState(isUserScrollActive)
    val currentAutoScrollActive = rememberUpdatedState(isAutoScrolling)
    val currentOnPositionChanged = rememberUpdatedState(onPositionChanged)

    LaunchedEffect(listState) {
        var settleJob: Job? = null
        listState.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Start -> {
                    settleJob?.cancel()
                    isUserScrollActive = true
                }
                is DragInteraction.Stop, is DragInteraction.Cancel -> {
                    settleJob?.cancel()
                    settleJob = launch {
                        snapshotFlow { listState.isScrollInProgress }.first { !it }
                        delay(USER_SCROLL_SETTLE_MS)
                        isUserScrollActive = false
                    }
                }
            }
        }
    }

    LaunchedEffect(listState, verses, itemIndexOffset) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(500)
            .collect { index ->
                if (!currentUserScrollActive.value && !currentAutoScrollActive.value) {
                    return@collect
                }
                val verse = verses.getOrNull(index - itemIndexOffset) ?: return@collect
                currentOnPositionChanged.value(verse.verseId, verse.number)
            }
    }
}
