package com.biblelib.feature.selection.view.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.biblelib.feature.selection.R

// Continues Bible selection with a wide, accessible floating action button.
@Composable
fun SelectionContinueFab(
    canProceed: Boolean,
    onProceed: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = { if (canProceed) onProceed() },
        modifier = Modifier.semantics { if (!canProceed) disabled() },
        shape = RoundedCornerShape(3.dp),
        containerColor = if (canProceed) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (canProceed) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
        text = { Text(stringResource(R.string.selection_continue)) },
        icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
    )
}
