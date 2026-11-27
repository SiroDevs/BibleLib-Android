package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblelib.feature.scripture_opener.opener.viewmodel.ExpandedField
import com.biblelib.feature.scripture_opener.opener.viewmodel.ScriptureSearchRowState

// Adapts the Scripture fields to split-pane and full-screen widths.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureFields(
    row: ScriptureSearchRowState,
    isSplitPane: Boolean,
    onToggleField: (ExpandedField) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val isPortraitLayout = isSplitPane || maxWidth < 600.dp
        Column(Modifier.fillMaxWidth()) {
            if (isPortraitLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookField(row, onToggleField, Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChapterField(row, onToggleField, Modifier.weight(1f))
                        VerseField(row, onToggleField, Modifier.weight(1f))
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BookField(row, onToggleField, Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_BOOK]))
                    ChapterField(row, onToggleField, Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_CHAPTER]))
                    VerseField(row, onToggleField, Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_VERSE]))
                }
            }
            content(isPortraitLayout)
        }
    }
}

@Composable
private fun BookField(row: ScriptureSearchRowState, onToggle: (ExpandedField) -> Unit, modifier: Modifier) {
    ScriptureField("Book", row.bookLabel, true, row.expanded == ExpandedField.BOOK, modifier) {
        onToggle(ExpandedField.BOOK)
    }
}

@Composable
private fun ChapterField(row: ScriptureSearchRowState, onToggle: (ExpandedField) -> Unit, modifier: Modifier) {
    ScriptureField("Chapter", row.chapterLabel, row.canExpandChapter,
        row.expanded == ExpandedField.CHAPTER, modifier) { onToggle(ExpandedField.CHAPTER) }
}

@Composable
private fun VerseField(row: ScriptureSearchRowState, onToggle: (ExpandedField) -> Unit, modifier: Modifier) {
    ScriptureField("Verse", row.verseLabel, row.canExpandVerse,
        row.expanded == ExpandedField.VERSE, modifier) { onToggle(ExpandedField.VERSE) }
}

@Composable
private fun ScriptureField(
    label: String,
    value: String,
    enabled: Boolean,
    isActive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            label = { Text(label) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                disabledContainerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                unfocusedBorderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                disabledBorderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                unfocusedLabelColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (enabled) {
            Box(
                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(4.dp)).clickable(onClick = onClick),
            )
        }
    }
}
