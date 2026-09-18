package com.biblelib.feature.bibles.view.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BibleEntity

private val RowShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableBibleRow(
    bible: BibleEntity,
    progress: Float?,
    isSecondary: Boolean,
    onDelete: () -> Unit,
    onToggleSecondary: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onContinueDownload: (() -> Unit)? = null,
    onRestartDownload: (() -> Unit)? = null,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> onToggleSecondary?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onToggleSecondary != null,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    alignment = Alignment.CenterEnd,
                )

                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = if (isSecondary) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                    contentDescription = if (isSecondary) "Remove from secondary" else "Add to secondary",
                    alignment = Alignment.CenterStart,
                )

                SwipeToDismissBoxValue.Settled -> {}
            }
        },
    ) {
        val downloadProgress = (progress ?: bible.downloadProgress).coerceIn(0f, 1f)

        Row(
            modifier = Modifier
                .clip(RowShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                Box(Modifier.padding(end = 4.dp)) { leadingContent() }
            }

            Box(Modifier.weight(1f)) {
                BiblePill(
                    bible = bible,
                    progress = downloadProgress,
                )
            }

            if (bible.downloadFailed) {
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    TextButton(
                        onClick = { onRestartDownload?.invoke() },
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Text("Restart")
                    }
                    TextButton(
                        onClick = { onContinueDownload?.invoke() },
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.ShimmerSweep() {
    val infiniteTransition = rememberInfiniteTransition(label = "pill_shimmer")
    val sweep by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pill_shimmer",
    )

    BoxWithConstraints(Modifier.matchParentSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        if (widthPx <= 0f) return@BoxWithConstraints

        val bandPx = widthPx * 0.35f
        val centerPx = sweep * widthPx

        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                        start = Offset(centerPx - bandPx, 0f),
                        end = Offset(centerPx + bandPx, 0f),
                    )
                )
        )
    }
}

@Composable
private fun SwipeActionBackground(
    color: Color,
    contentColor: Color,
    icon: ImageVector,
    contentDescription: String,
    alignment: Alignment,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(icon, contentDescription, tint = contentColor)
    }
}
