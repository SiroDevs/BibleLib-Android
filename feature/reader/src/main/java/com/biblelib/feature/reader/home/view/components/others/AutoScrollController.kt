package com.biblelib.feature.reader.home.view.components.others

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val AUTO_SCROLL_BASE_PIXELS_PER_TICK = 2.5f
private const val AUTO_SCROLL_TICK_MS = 16L
private const val AUTO_SCROLL_MIN_SPEED = 0.25f
private const val AUTO_SCROLL_MAX_SPEED = 4f
private const val AUTO_SCROLL_SPEED_STEP = 0.25f

@Stable
class AutoScrollController(private val listState: LazyListState) {
    var isAutoScrolling by mutableStateOf(false)
        private set
    var speedMultiplier by mutableFloatStateOf(1f)
        private set

    fun toggle() {
        isAutoScrolling = !isAutoScrolling
    }

    fun speedUp() {
        speedMultiplier = (speedMultiplier + AUTO_SCROLL_SPEED_STEP).coerceAtMost(AUTO_SCROLL_MAX_SPEED)
    }

    fun speedDown() {
        speedMultiplier = (speedMultiplier - AUTO_SCROLL_SPEED_STEP).coerceAtLeast(AUTO_SCROLL_MIN_SPEED)
    }

    internal suspend fun runWhileActive() {
        if (!isAutoScrolling) return
        while (currentCoroutineContext().isActive && listState.canScrollForward) {
            listState.scrollBy(AUTO_SCROLL_BASE_PIXELS_PER_TICK * speedMultiplier)
            delay(AUTO_SCROLL_TICK_MS)
        }
        isAutoScrolling = false
    }
}

@Composable
fun rememberAutoScrollController(listState: LazyListState): AutoScrollController {
    val controller = remember(listState) { AutoScrollController(listState) }
    LaunchedEffect(controller.isAutoScrolling, controller.speedMultiplier) {
        controller.runWhileActive()
    }
    return controller
}