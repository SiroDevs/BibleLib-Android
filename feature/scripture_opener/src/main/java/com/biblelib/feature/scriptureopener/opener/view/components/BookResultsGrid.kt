package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BookEntity
import com.biblelib.core.ui.utils.isLandscape
import com.biblelib.feature.scripture_opener.R

private const val OT_BOOK_COUNT = 39
private const val PORTRAIT_COLUMNS_PER_TESTAMENT = 2
private val LANDSCAPE_MIN_CELL_WIDTH = 64.dp
private val PORTRAIT_GRID_HEIGHT = 320.dp
private val LANDSCAPE_GRID_HEIGHT = 240.dp

// Old and New Testament book lists side by side, each scrolling on its own
@Composable
fun BookResultsGrid(
    books: List<BookEntity>,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ordered = remember(books) { books.sortedBy { it.sortOrder } }
    val oldTestament = remember(ordered) { ordered.take(OT_BOOK_COUNT) }
    val newTestament = remember(ordered) { ordered.drop(OT_BOOK_COUNT) }

    val landscape = isLandscape()
    val columns = if (landscape) {
        GridCells.Adaptive(LANDSCAPE_MIN_CELL_WIDTH)
    } else {
        GridCells.Fixed(PORTRAIT_COLUMNS_PER_TESTAMENT)
    }
    val gridHeight: Dp = if (landscape) LANDSCAPE_GRID_HEIGHT else PORTRAIT_GRID_HEIGHT

    Column(modifier = modifier.fillMaxWidth()) {
        FieldPointerArrow(fieldIndex = FIELD_INDEX_BOOK)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(gridHeight)) {
                TestamentPane(
                    title = stringResource(R.string.old_testament),
                    titleColor = MaterialTheme.colorScheme.primary,
                    shade = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                    books = oldTestament,
                    columns = columns,
                    selectedBookId = selectedBookId,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f),
                )
                TestamentPane(
                    title = stringResource(R.string.new_testament),
                    titleColor = MaterialTheme.colorScheme.tertiary,
                    shade = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f),
                    books = newTestament,
                    columns = columns,
                    selectedBookId = selectedBookId,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TestamentPane(
    title: String,
    titleColor: Color,
    shade: Color,
    books: List<BookEntity>,
    columns: GridCells,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(shade)) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        HorizontalDivider(thickness = 0.5.dp)
        LazyVerticalGrid(columns = columns, modifier = Modifier.fillMaxSize()) {
            items(books, key = { it.id }) { book ->
                BookCell(book, selectedBookId, onSelect)
            }
        }
    }
}

@Composable
private fun BookCell(
    book: BookEntity,
    selectedBookId: String?,
    onSelect: (BookEntity) -> Unit,
) {
    val isSelected = book.id == selectedBookId
    Surface(
        modifier = Modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onSelect(book) },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
            Text(
                text = book.abbreviation.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
