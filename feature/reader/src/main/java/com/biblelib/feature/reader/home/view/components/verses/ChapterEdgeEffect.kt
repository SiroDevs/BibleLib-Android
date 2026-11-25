package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.biblelib.feature.reader.home.utils.ChapterEdge
import com.biblelib.feature.reader.home.utils.NEXT_CHAPTER_KEY
import com.biblelib.feature.reader.home.utils.PREV_CHAPTER_KEY
import kotlinx.coroutines.flow.distinctUntilChanged

// Reports when the list is scrolled fully onto the given chapter edge, and when it leaves it
@Composable
fun ChapterEdgeEffect(
    edge: ChapterEdge,
    enabled: Boolean,
    listState: LazyListState,
    chapterId: String?,
    onReached: (ChapterEdge) -> Unit,
    onLeft: (ChapterEdge) -> Unit,
) {
    val currentOnReached by rememberUpdatedState(onReached)
    val currentOnLeft by rememberUpdatedState(onLeft)

    LaunchedEffect(listState, enabled, chapterId) {
        if (!enabled) return@LaunchedEffect
        snapshotFlow { listState.isAtEdge(edge) }
            .distinctUntilChanged()
            .collect { atEdge ->
                if (atEdge) currentOnReached(edge) else currentOnLeft(edge)
            }
    }
}

private fun LazyListState.isAtEdge(edge: ChapterEdge): Boolean = when (edge) {
    ChapterEdge.START ->
        layoutInfo.visibleItemsInfo.firstOrNull()?.key == PREV_CHAPTER_KEY && !canScrollBackward

    ChapterEdge.END ->
        layoutInfo.visibleItemsInfo.lastOrNull()?.key == NEXT_CHAPTER_KEY && !canScrollForward
}
