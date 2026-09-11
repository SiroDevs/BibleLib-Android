package com.biblelib.feature.reader.main.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblelib.core.common.entity.VerseDisplay
import com.biblelib.feature.reader.main.utils.SWIPE_ACTION_TRIGGER_PX
import kotlin.math.roundToInt

@Composable
fun VerseRow(
    verse: VerseDisplay,
    fontSizeSp: Float,
    fontFamily: FontFamily,
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
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    val rowBackground = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        bookmarkColor != null -> runCatching { Color(android.graphics.Color.parseColor(bookmarkColor)) }
            .getOrDefault(Color.Transparent).copy(alpha = 0.35f)

        else -> Color.Transparent
    }

    val highlightColor = MaterialTheme.colorScheme.secondaryContainer

    Box(modifier = Modifier.fillMaxWidth()) {
        // Background reveal icons behind the swiping row.
        Box(modifier = Modifier.fillMaxWidth()) {
            if (offsetX > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Bookmark",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else if (offsetX < 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Notes",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
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
                            }
                        )
                    }
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = onLongPress,
                    onClick = { if (isSelectionMode) onTap() },
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            // Primary verse
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp * 0.72f).sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    ) { append("${verse.number} ") }
                    if (isBookmarked && bookmarkColor == null) {
                        append("\uD83D\uDD16 ") // quick-bookmark icon glyph inline
                    }
                    appendHighlighted(verse.text, highlightQuery, highlightColor)
                    if (hasNote) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(" \uD83D\uDCDD")
                        }
                    }
                },
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.6f).sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onBackground,
            )

            AnimatedVisibility(visible = parallelTexts.values.any { it.isNotEmpty() }) {
                Column {
                    parallelTexts.forEach { (abbr, text) ->
                        if (text.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (fontSizeSp * 0.65f).sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                        )
                                    ) { append("[${abbr.uppercase()}] ") }
                                    appendHighlighted(text, highlightQuery, highlightColor)
                                },
                                fontSize = (fontSizeSp * 0.85f).sp,
                                lineHeight = (fontSizeSp * 1.5f).sp,
                                fontFamily = fontFamily,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendHighlighted(
    text: String,
    query: String?,
    highlightColor: Color,
) {
    if (query.isNullOrBlank()) {
        append(text)
        return
    }
    val lower = text.lowercase()
    val qLower = query.lowercase()
    var start = 0
    var idx = lower.indexOf(qLower)
    while (idx >= 0) {
        append(text.substring(start, idx))
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                background = highlightColor,
            )
        ) {
            append(text.substring(idx, idx + query.length))
        }
        start = idx + query.length
        idx = lower.indexOf(qLower, start)
    }
    append(text.substring(start))
}
