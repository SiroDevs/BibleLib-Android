package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.feature.scripture_opener.opener.viewmodel.ExpandedField
import com.biblelib.feature.scripture_opener.opener.viewmodel.ScriptureSearchRowState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureSearch(
    row: ScriptureSearchRowState,
    isActive: Boolean,
    onToggleField: (ExpandedField) -> Unit,
    onSelectBook: (BookEntity) -> Unit,
    onSelectChapter: (ChapterEntity) -> Unit,
    onSelectVerse: (Int) -> Unit,
    onOpenScripture: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToQueueAndClose: () -> Unit,
    onAddToQueueAndFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (row.locked) {
        LockedScriptureRow(row = row)
        return
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ScriptureField(
                label = "Book",
                value = row.bookLabel,
                enabled = true,
                isActive = row.expanded == ExpandedField.BOOK,
                modifier = Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_BOOK]),
                onClick = { onToggleField(ExpandedField.BOOK) },
            )
            ScriptureField(
                label = "Chapter",
                value = row.chapterLabel,
                enabled = row.canExpandChapter,
                isActive = row.expanded == ExpandedField.CHAPTER,
                modifier = Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_CHAPTER]),
                onClick = { onToggleField(ExpandedField.CHAPTER) },
            )
            ScriptureField(
                label = "Verse",
                value = row.verseLabel,
                enabled = row.canExpandVerse,
                isActive = row.expanded == ExpandedField.VERSE,
                modifier = Modifier.weight(FIELD_WEIGHTS[FIELD_INDEX_VERSE]),
                onClick = { onToggleField(ExpandedField.VERSE) },
            )
        }

        AnimatedVisibility(visible = row.expanded == ExpandedField.BOOK, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            BookResultsGrid(
                books = row.books,
                selectedBookId = row.selectedBook?.id,
                onSelect = onSelectBook,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        AnimatedVisibility(visible = row.expanded == ExpandedField.CHAPTER, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            if (row.isLoadingChapters) {
                LoadingRow()
            } else {
                ChapterResultsGrid(
                    chapters = row.chapters,
                    selectedChapterId = row.selectedChapter?.id,
                    onSelect = onSelectChapter,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        AnimatedVisibility(visible = row.expanded == ExpandedField.VERSE, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            if (row.isLoadingVerses) {
                LoadingRow()
            } else {
                VerseResultsGrid(
                    verseCount = row.verses.size,
                    selectedVerseNumber = row.selectedVerseNumber,
                    onSelect = onSelectVerse,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = isActive && row.isComplete && row.expanded == ExpandedField.NONE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ScriptureActionButtons(
                reference = row.reference,
                onOpenScripture = onOpenScripture,
                onAddToQueue = onAddToQueue,
                onAddToQueueAndClose = onAddToQueueAndClose,
                onAddToQueueAndFinish = onAddToQueueAndFinish,
            )
        }
    }
}

@Composable
private fun ScriptureField(
    label: String,
    value: String,
    enabled: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onClick)
            )
        }
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .height(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
