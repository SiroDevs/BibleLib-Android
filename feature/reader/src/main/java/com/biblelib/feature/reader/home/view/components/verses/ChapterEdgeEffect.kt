package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import com.biblelib.feature.reader.home.utils.ChapterEdge
import kotlinx.coroutines.flow.distinctUntilChanged

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
        layoutInfo.visibleItemsInfo.firstOrNull()?.index == 0 && !canScrollBackward

    ChapterEdge.END ->
        layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1 && !canScrollForward
}
