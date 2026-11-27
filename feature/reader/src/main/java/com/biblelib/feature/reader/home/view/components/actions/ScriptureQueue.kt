package com.biblelib.feature.reader.home.view.components.actions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.biblelib.feature.reader.R
import com.biblelib.core.database.entities.ScriptureItemEntity
import com.biblelib.feature.reader.home.utils.ReaderUiState
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

@Composable
// Splits queued scripture links around a vertical fold when available.
fun ScriptureQueue(
    state: ReaderUiState,
    viewModel: ReaderViewModel,
    onQuickSettings: () -> Unit,
    modifier: Modifier = Modifier,
    hingeGutter: Dp? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        val rowModifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
        if (hingeGutter == null) {
            Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                ScriptureQueueItems(
                    items = state.queueItems,
                    activeItemId = state.queueActiveItemId,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                )
                QueueActions(onQuickSettings, viewModel::dismissScriptureQueue)
            }
        } else {
            val splitIndex = (state.queueItems.size + 1) / 2
            Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                ScriptureQueueItems(
                    items = state.queueItems.take(splitIndex),
                    activeItemId = state.queueActiveItemId,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(hingeGutter))
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ScriptureQueueItems(
                        items = state.queueItems.drop(splitIndex),
                        activeItemId = state.queueActiveItemId,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f),
                    )
                    QueueActions(onQuickSettings, viewModel::dismissScriptureQueue)
                }
            }
        }
    }
}

@Composable
// Shows the queue entries assigned to one side of a foldable.
private fun ScriptureQueueItems(
    items: List<ScriptureItemEntity>,
    activeItemId: Long?,
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        items(items, key = { it.id }) { item ->
            val isActive = item.id == activeItemId
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { viewModel.jumpToQueueItem(item) },
            ) {
                Text(
                    text = "${item.bookAbbr.uppercase()} ${item.chapterNumber}:${item.verseNumber}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
// Groups quick settings and queue dismissal actions.
private fun QueueActions(onQuickSettings: () -> Unit, onDismiss: () -> Unit) {
    IconButton(onClick = onQuickSettings) {
        Icon(
            Icons.Default.Tune,
            contentDescription = stringResource(R.string.reader_quick_settings_content_description),
        )
    }
    IconButton(onClick = onDismiss) {
        Icon(
            Icons.Default.Close,
            contentDescription = stringResource(
                R.string.reader_close_scripture_queue_content_description,
            ),
        )
    }
}
