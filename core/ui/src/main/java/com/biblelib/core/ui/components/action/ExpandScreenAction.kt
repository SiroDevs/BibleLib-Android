package com.biblelib.core.ui.components.action

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.biblelib.core.ui.R

// Expands a split-pane destination to the full window.
@Composable
fun ExpandScreenAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.OpenInFull,
            contentDescription = stringResource(R.string.adaptive_open_full_screen),
        )
    }
}
