package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    isSplitPane: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (row.locked) {
        LockedScriptureRow(row = row)
        return
    }

    ScriptureFields(
        row = row,
        isSplitPane = isSplitPane,
        onToggleField = onToggleField,
        modifier = modifier.padding(vertical = 6.dp),
    ) { isPortraitLayout ->
        AnimatedVisibility(visible = row.expanded == ExpandedField.BOOK, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            BookResultsGrid(
                books = row.books,
                selectedBookId = row.selectedBook?.id,
                onSelect = onSelectBook,
                isPortraitLayout = isPortraitLayout,
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
                    isPortraitLayout = isPortraitLayout,
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
                    isPortraitLayout = isPortraitLayout,
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
