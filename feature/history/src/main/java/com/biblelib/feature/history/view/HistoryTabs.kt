package com.biblelib.feature.history.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.database.entities.HistoryEntity
import com.biblelib.core.database.entities.SearchEntity
import com.biblelib.feature.history.R
import com.biblelib.feature.history.viewmodel.HistoryGroup
import java.text.SimpleDateFormat
import java.util.Date

// Displays grouped reading history and opens selected passages in the reader.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingHistoryTab(
    groups: List<HistoryGroup>,
    isLoading: Boolean,
    navController: NavController,
    onOpenInReader: ((String, String, String, String, String) -> Unit)?,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (groups.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.history_no_reading),
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
            )
        }
        return
    }

    LazyColumn {
        groups.forEach { group ->
            stickyHeader(key = group.dateLabel) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = group.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(group.entries, key = { it.id }) { entry ->
                HistoryEntryItem(entry) {
                    if (onOpenInReader != null) {
                        onOpenInReader(entry.bibleAbbr, entry.bookId, entry.chapterId, "", "")
                    } else {
                        navController.navigate(
                            Routes.reader(bibleAbbr = entry.bibleAbbr, bookId = entry.bookId, chapterId = entry.chapterId)
                        ) {
                            popUpTo(Routes.READER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

// Displays saved searches in chronological order.
@Composable
fun SearchHistoryTab(history: List<SearchEntity>, isLoading: Boolean) {
    if (isLoading) return
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.history_no_searches),
                color = MaterialTheme.colorScheme.onSurface.copy(0.4f),
            )
        }
        return
    }
    LazyColumn {
        items(history, key = { it.id }) { search ->
            val format = SimpleDateFormat("MMM d, h:mm a", LocalLocale.current.platformLocale)
            ListItem(
                headlineContent = { Text(search.qry) },
                supportingContent = { Text(format.format(Date(search.queriedAt))) },
                leadingContent = {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                },
                modifier = Modifier.clickable {},
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

@Composable
private fun HistoryEntryItem(entry: HistoryEntity, onClick: () -> Unit) {
    val format = SimpleDateFormat("h:mm a", LocalLocale.current.platformLocale)
    val verseLabel = entry.verseNumber?.let { ":$it" } ?: ""
    ListItem(
        headlineContent = { Text("${entry.chapterRef}$verseLabel") },
        supportingContent = {
            Text(
                "${entry.bibleName.ifBlank { entry.bibleAbbr.uppercase() }} · ${format.format(Date(entry.readAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        },
        leadingContent = {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
