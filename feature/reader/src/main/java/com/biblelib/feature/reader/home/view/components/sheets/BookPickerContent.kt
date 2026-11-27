package com.biblelib.feature.reader.home.view.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.database.entities.ChapterEntity
import com.biblelib.feature.reader.R
import com.biblelib.feature.reader.home.utils.ReaderUiState

private const val OLD_TESTAMENT_BOOK_COUNT = 39

@Composable
// Shows searchable Old and New Testament book lists.
fun BookPickerContent(
    state: ReaderUiState,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
    modifier: Modifier = Modifier,
    listHeight: Dp = 480.dp,
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val orderedBooks = remember(state.books) { state.books.sortedBy { it.sortOrder } }
    val tabBooks = if (selectedTab == 0) {
        orderedBooks.take(OLD_TESTAMENT_BOOK_COUNT)
    } else {
        orderedBooks.drop(OLD_TESTAMENT_BOOK_COUNT)
    }
    val filteredBooks = remember(tabBooks, query) {
        if (query.isBlank()) tabBooks else tabBooks.filter { book ->
            book.name.contains(query, ignoreCase = true) ||
                    book.nameLong.contains(query, ignoreCase = true) ||
                    book.abbreviation.contains(query, ignoreCase = true)
        }
    }

    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            placeholder = { Text(stringResource(R.string.reader_search_books)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
        )
        PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(top = 8.dp)) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.reader_old_testament)) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.reader_new_testament)) },
            )
        }
        LazyColumn(
            modifier = Modifier.heightIn(max = listHeight),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(filteredBooks, key = { it.id }) { book ->
                val isSelected = book.id == selectedBookId
                ListItem(
                    headlineContent = { Text(book.name) },
                    leadingContent = {
                        Text(
                            book.abbreviation.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelect(book) },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
// Combines book selection and chapter navigation for a persistent pane.
fun BookNavigationPane(
    state: ReaderUiState,
    onSelectBook: (BookEntity) -> Unit,
    onSelectChapter: (ChapterEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val listHeight = (maxHeight - 440.dp).coerceIn(120.dp, 360.dp)
        Column(
            Modifier.fillMaxWidth().fillMaxHeight().background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = stringResource(R.string.reader_books_and_chapters),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            BookPickerContent(
                state = state,
                selectedBookId = state.activeBook?.id,
                onSelect = onSelectBook,
                listHeight = listHeight,
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.reader_chapters),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                modifier = Modifier.heightIn(max = 176.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                items(state.chapters, key = { it.id }) { chapter ->
                    val isSelected = chapter.id == state.activeChapter?.id
                    Surface(
                        modifier = Modifier
                            .padding(3.dp)
                            .clickable { onSelectChapter(chapter) },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = chapter.number,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
