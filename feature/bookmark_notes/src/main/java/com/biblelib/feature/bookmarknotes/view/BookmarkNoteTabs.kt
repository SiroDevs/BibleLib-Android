package com.biblelib.feature.bookmark_notes.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BookmarkEntity
import com.biblelib.core.database.entities.NoteEntity
import com.biblelib.feature.bookmark_notes.R

// Shows saved bookmarks with selection and navigation actions.
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookmarksTab(
    bookmarks: List<BookmarkEntity>,
    isLoading: Boolean,
    selectedKeys: Set<String>,
    onTap: (BookmarkEntity) -> Unit,
    onLongPress: (BookmarkEntity) -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (bookmarks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.bookmark_no_bookmarks),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
        return
    }
    LazyColumn {
        items(bookmarks, key = { "${it.bibleAbbr}|${it.verseId}" }) { item ->
            val key = "${item.bibleAbbr}|${item.verseId}"
            val selected = key in selectedKeys
            ListItem(
                headlineContent = { Text("${item.bookId} — ${item.chapterId.substringAfter(".")}") },
                supportingContent = {
                    Text(item.bibleAbbr.uppercase(), style = MaterialTheme.typography.labelSmall)
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Bookmark,
                        null,
                        tint = item.colorHex?.let {
                            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
                        } ?: MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { onTap(item) },
                    onLongClick = { onLongPress(item) },
                ),
                colors = selectedListItemColors(selected),
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

// Shows saved notes with selection and navigation actions.
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NotesTab(
    notes: List<NoteEntity>,
    isLoading: Boolean,
    selectedKeys: Set<String>,
    onTap: (NoteEntity) -> Unit,
    onLongPress: (NoteEntity) -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (notes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.bookmark_no_notes),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
        return
    }
    LazyColumn {
        items(notes, key = { "${it.bibleAbbr}|${it.verseId}" }) { item ->
            val key = "${item.bibleAbbr}|${item.verseId}"
            val selected = key in selectedKeys
            ListItem(
                headlineContent = { Text(item.title.ifBlank { item.verseText.take(40) }) },
                supportingContent = {
                    Text(item.noteText, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                },
                leadingContent = {
                    Icon(Icons.AutoMirrored.Filled.StickyNote2, null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = { onTap(item) },
                    onLongClick = { onLongPress(item) },
                ),
                colors = selectedListItemColors(selected),
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

// Highlights list rows that are selected for bulk actions.
@Composable
private fun selectedListItemColors(selected: Boolean) =
    if (selected) {
        androidx.compose.material3.ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
    } else {
        androidx.compose.material3.ListItemDefaults.colors()
    }
