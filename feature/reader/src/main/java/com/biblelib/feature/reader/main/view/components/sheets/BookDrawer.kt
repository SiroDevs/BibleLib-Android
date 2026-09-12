package com.biblelib.feature.reader.main.view.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.feature.reader.main.utils.ReaderUiState

private const val OT_BOOK_COUNT = 39

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDrawer(
    state: ReaderUiState,
    onSelect: (BookEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val orderedBooks = remember(state.books) { state.books.sortedBy { it.sortOrder } }
    val oldTestament = remember(orderedBooks) { orderedBooks.take(OT_BOOK_COUNT) }
    val newTestament = remember(orderedBooks) { orderedBooks.drop(OT_BOOK_COUNT) }

    val tabBooks = if (selectedTab == 0) oldTestament else newTestament
    val filteredBooks = remember(tabBooks, query) {
        if (query.isBlank()) {
            tabBooks
        } else {
            tabBooks.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.nameLong.contains(query, ignoreCase = true) ||
                    it.abbreviation.contains(query, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search books...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
        )

        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(top = 12.dp)) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Old Testament") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("New Testament") },
            )
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            items(filteredBooks, key = { it.id }) { book ->
                ListItem(
                    headlineContent = { Text(book.name) },
                    leadingContent = {
                        Text(
                            book.id.take(3),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.clickable { onSelect(book) },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}
