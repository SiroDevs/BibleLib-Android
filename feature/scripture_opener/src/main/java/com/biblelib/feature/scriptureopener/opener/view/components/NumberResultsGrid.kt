package com.biblelib.feature.scripture_opener.opener.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.ChapterEntity

@Composable
fun ChapterResultsGrid(
    chapters: List<ChapterEntity>,
    selectedChapterId: String?,
    onSelect: (ChapterEntity) -> Unit,
    isPortraitLayout: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldPointerArrow(fieldIndex = FIELD_INDEX_CHAPTER, isPortraitLayout = isPortraitLayout)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(10.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.heightIn(max = 280.dp),
                contentPadding = PaddingValues(6.dp),
            ) {
                items(chapters, key = { it.id }) { chapter ->
                    NumberCell(
                        label = chapter.number,
                        isSelected = chapter.id == selectedChapterId,
                        onClick = { onSelect(chapter) },
                    )
                }
            }
        }
    }
}

@Composable
fun VerseResultsGrid(
    verseCount: Int,
    selectedVerseNumber: Int?,
    onSelect: (Int) -> Unit,
    isPortraitLayout: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldPointerArrow(fieldIndex = FIELD_INDEX_VERSE, isPortraitLayout = isPortraitLayout)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(10.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.heightIn(max = 280.dp),
                contentPadding = PaddingValues(6.dp),
            ) {
                items(verseCount) { index ->
                    val number = index + 1
                    NumberCell(
                        label = number.toString(),
                        isSelected = number == selectedVerseNumber,
                        onClick = { onSelect(number) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberCell(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
