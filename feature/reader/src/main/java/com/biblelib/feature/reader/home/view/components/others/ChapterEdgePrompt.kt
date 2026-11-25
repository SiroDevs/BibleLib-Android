package com.biblelib.feature.reader.home.view.components.others

import android.content.Context
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.biblelib.feature.reader.R
import com.biblelib.feature.reader.home.utils.ChapterEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Asks the reader whether to move to the neighbouring chapter once a chapter edge is reached
@Stable
class ChapterEdgePrompt internal constructor(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val context: Context,
    private val onAccept: (direction: Int) -> Unit,
) {
    private var promptJob: Job? = null
    private var promptEdge: ChapterEdge? = null

    fun show(edge: ChapterEdge, chapterLabel: String) {
        promptJob?.cancel()
        promptEdge = edge
        promptJob = scope.launch {
            val messageRes = when (edge) {
                ChapterEdge.START -> R.string.chapter_edge_start
                ChapterEdge.END -> R.string.chapter_edge_end
            }
            val result = hostState.showSnackbar(
                message = context.getString(messageRes, chapterLabel),
                actionLabel = context.getString(R.string.chapter_edge_accept),
                duration = SnackbarDuration.Indefinite,
            )
            if (result == SnackbarResult.ActionPerformed) onAccept(edge.direction)
        }
    }

    // Withdraws the prompt when the reader scrolls away from the edge that raised it
    fun hide(edge: ChapterEdge) {
        if (promptEdge == edge) dismiss()
    }

    fun dismiss() {
        promptJob?.cancel()
        promptJob = null
        promptEdge = null
    }
}

@Composable
fun rememberChapterEdgePrompt(
    hostState: SnackbarHostState,
    chapterId: String?,
    onAccept: (direction: Int) -> Unit,
): ChapterEdgePrompt {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnAccept by rememberUpdatedState(onAccept)
    val prompt = remember(hostState, context) {
        ChapterEdgePrompt(hostState, scope, context) { currentOnAccept(it) }
    }
    LaunchedEffect(chapterId) { prompt.dismiss() }
    return prompt
}

// Snackbar host that shows ACCEPT / IGNORE buttons for prompts that carry an action
@Composable
fun ReaderSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        val actionLabel = data.visuals.actionLabel
        Snackbar(
            action = actionLabel?.let { label ->
                { TextButton(onClick = data::performAction) { Text(label) } }
            },
            dismissAction = actionLabel?.let {
                {
                    TextButton(onClick = data::dismiss) {
                        Text(stringResource(R.string.chapter_edge_ignore))
                    }
                }
            },
        ) { Text(data.visuals.message) }
    }
}
