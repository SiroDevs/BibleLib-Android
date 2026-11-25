package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.biblelib.core.ui.utils.isLandscape
import com.biblelib.feature.scripture_opener.R

// Actions for a completed scripture row: one row in landscape, stacked rows in portrait
@Composable
fun ScriptureActionButtons(
    reference: String,
    onOpenScripture: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToQueueAndClose: () -> Unit,
    onAddToQueueAndFinish: () -> Unit,
) {
    val landscape = isLandscape()
    val openLabel = if (landscape) {
        stringResource(R.string.open_scripture_short)
    } else {
        stringResource(R.string.open_scripture, reference)
    }

    if (landscape) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OpenButton(openLabel, onOpenScripture, Modifier.weight(1f))
            QueueButton(Icons.Default.PlaylistAdd, stringResource(R.string.add_to_queue), onAddToQueue, Modifier.weight(1f))
            QueueButton(Icons.Default.Close, stringResource(R.string.queue_and_close), onAddToQueueAndClose, Modifier.weight(1f))
            QueueButton(Icons.Default.Done, stringResource(R.string.queue_and_finish), onAddToQueueAndFinish, Modifier.weight(1f))
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OpenButton(openLabel, onOpenScripture, Modifier.fillMaxWidth())
        QueueButton(Icons.Default.PlaylistAdd, stringResource(R.string.add_to_queue), onAddToQueue, Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QueueButton(Icons.Default.Close, stringResource(R.string.queue_and_close), onAddToQueueAndClose, Modifier.weight(1f))
            QueueButton(Icons.Default.Done, stringResource(R.string.queue_and_finish), onAddToQueueAndFinish, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OpenButton(label: String, onClick: () -> Unit, modifier: Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Icon(Icons.Default.MenuBook, null)
        Spacer(Modifier.width(8.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QueueButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Icon(icon, null)
        Spacer(Modifier.width(4.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
