package com.biblelib.feature.reader.home.view.components.verses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblelib.core.common.entity.VerseDisplay

@Composable
// Arranges primary and parallel Bible text for the available width.
internal fun VerseBody(
    verse: VerseDisplay,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    highlightQuery: String?,
    parallelTexts: Map<String, String>,
    isBookmarked: Boolean,
    hasNote: Boolean,
    sideBySideParallelText: Boolean,
    parallelTextGutter: Dp,
) {
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val visibleParallelTexts = parallelTexts.filterValues(String::isNotEmpty)
    if (sideBySideParallelText && visibleParallelTexts.isNotEmpty()) {
        Row(Modifier.fillMaxWidth()) {
            PrimaryVerseText(
                verse = verse,
                fontSizeSp = fontSizeSp,
                fontFamily = fontFamily,
                highlightQuery = highlightQuery,
                highlightColor = highlightColor,
                isBookmarked = isBookmarked,
                hasNote = hasNote,
                modifier = Modifier.weight(1f).padding(end = parallelTextGutter / 2),
            )
            Column(Modifier.weight(1f).padding(start = parallelTextGutter / 2)) {
                ParallelVerseTexts(
                    texts = visibleParallelTexts,
                    fontSizeSp = fontSizeSp,
                    fontFamily = fontFamily,
                    highlightQuery = highlightQuery,
                    highlightColor = highlightColor,
                )
            }
        }
    } else {
        Column(Modifier.fillMaxWidth()) {
            PrimaryVerseText(
                verse = verse,
                fontSizeSp = fontSizeSp,
                fontFamily = fontFamily,
                highlightQuery = highlightQuery,
                highlightColor = highlightColor,
                isBookmarked = isBookmarked,
                hasNote = hasNote,
            )
            AnimatedVisibility(visibleParallelTexts.isNotEmpty()) {
                Column {
                    ParallelVerseTexts(
                        texts = visibleParallelTexts,
                        fontSizeSp = fontSizeSp,
                        fontFamily = fontFamily,
                        highlightQuery = highlightQuery,
                        highlightColor = highlightColor,
                    )
                }
            }
        }
    }
}

@Composable
// Renders a verse with reference, bookmark, note, and search highlights.
private fun PrimaryVerseText(
    verse: VerseDisplay,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    highlightQuery: String?,
    highlightColor: Color,
    isBookmarked: Boolean,
    hasNote: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = (fontSizeSp * 0.72f).sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            ) { append("${verse.number} ") }
            if (isBookmarked) append("🔖 ")
            appendHighlighted(verse.text, highlightQuery, highlightColor)
            if (hasNote) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                    append(" 📝")
                }
            }
        },
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.6f).sp,
        fontFamily = fontFamily,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
// Renders all parallel translation text for one verse.
private fun ParallelVerseTexts(
    texts: Map<String, String>,
    fontSizeSp: Float,
    fontFamily: FontFamily,
    highlightQuery: String?,
    highlightColor: Color,
) {
    texts.forEach { (abbreviation, text) ->
        Spacer(Modifier.height(2.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp * 0.65f).sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                ) { append("[${abbreviation.uppercase()}] ") }
                appendHighlighted(text, highlightQuery, highlightColor)
            },
            fontSize = (fontSizeSp * 0.85f).sp,
            lineHeight = (fontSizeSp * 1.5f).sp,
            fontFamily = fontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        )
    }
}

// Applies search highlighting while preserving the original text.
private fun AnnotatedString.Builder.appendHighlighted(
    text: String,
    query: String?,
    highlightColor: Color,
) {
    if (query.isNullOrBlank()) {
        append(text)
        return
    }
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var startIndex = 0
    var matchIndex = lowerText.indexOf(lowerQuery)
    while (matchIndex >= 0) {
        append(text.substring(startIndex, matchIndex))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, background = highlightColor)) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }
        startIndex = matchIndex + query.length
        matchIndex = lowerText.indexOf(lowerQuery, startIndex)
    }
    append(text.substring(startIndex))
}
