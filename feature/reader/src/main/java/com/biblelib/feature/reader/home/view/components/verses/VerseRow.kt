package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.feature.reader.home.utils.SWIPE_ACTION_TRIGGER_PX
import kotlin.math.roundToInt

// Displays one verse with selection, annotation, and swipe actions.
@Composable
fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    highlightQuery: String?,
    parallelTexts: Map<String, String>,
    bookmarkColor: String?,
    isBookmarked: Boolean,
    hasNote: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongPress: () -> Unit,
    onTap: () -> Unit,
    onSwipeBookmark: () -> Unit,
    onSwipeNotes: () -> Unit,
    sideBySideParallelText: Boolean = false,
    parallelTextGutter: androidx.compose.ui.unit.Dp = 24.dp,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        bookmarkColor != null -> runCatching { Color(bookmarkColor.toColorInt()) }
            .getOrDefault(Color.Transparent).copy(alpha = 0.35f)

        else -> Color.Transparent
    }

    Box(Modifier.fillMaxWidth()) {
        if (offsetX != 0f) {
            Icon(
                imageVector = if (offsetX > 0f) Icons.Default.Bookmark else Icons.Default.EditNote,
                contentDescription = null,
                tint = if (offsetX > 0f) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(if (offsetX > 0f) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 12.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clip(RoundedCornerShape(6.dp))
                .background(rowBackground)
                .pointerInput(isSelectionMode) {
                    if (!isSelectionMode) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    offsetX >= SWIPE_ACTION_TRIGGER_PX -> onSwipeBookmark()
                                    offsetX <= -SWIPE_ACTION_TRIGGER_PX -> onSwipeNotes()
                                }
                                offsetX = 0f
                            },
                            onDragCancel = { offsetX = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount).coerceIn(-220f, 220f)
                            },
                        )
                    }
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = onLongPress,
                    onClick = { if (isSelectionMode) onTap() },
                )
                .padding(vertical = 6.dp, horizontal = 4.dp),
        ) {
            VerseBody(
                verse = verse,
                fontSizeSp = fontSizeSp,
                fontFamily = fontFamily,
                highlightQuery = highlightQuery,
                parallelTexts = parallelTexts,
                isBookmarked = isBookmarked && bookmarkColor == null,
                hasNote = hasNote,
                sideBySideParallelText = sideBySideParallelText,
                parallelTextGutter = parallelTextGutter,
            )
        }
    }
}
