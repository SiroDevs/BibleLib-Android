package com.biblelib.feature.history.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.database.entities.HistoryEntity
import com.biblelib.feature.history.viewmodel.HistoryGroup
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.action.ExpandScreenAction
import com.biblelib.core.ui.components.layout.CenteredContentFrame
import com.biblelib.feature.history.R
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
// Shows reading and search history with navigation back to matching content.
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
    onExpand: (() -> Unit)? = null,
) {
    val state   by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.history_title),
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    onExpand?.let { ExpandScreenAction(it) }
                    IconButton(onClick = {
                        if (activeTab == 0) viewModel.clearReadingHistory()
                        else viewModel.clearSearchHistory()
                    }) {
                        Icon(Icons.Default.DeleteSweep, stringResource(R.string.history_clear),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
            )
        }
    ) { padding ->
        CenteredContentFrame(
            modifier = Modifier.fillMaxSize().padding(padding),
            respectVerticalHinge = false,
        ) {
            Column(Modifier.fillMaxSize()) {
                PrimaryTabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text(stringResource(R.string.history_reading_tab)) },
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text(stringResource(R.string.history_searches_tab)) },
                    )
                }

                when (activeTab) {
                    0 -> ReadingHistoryTab(
                        groups = state.readingHistory,
                        isLoading = state.isLoading,
                        navController = navController,
                    )
                    1 -> SearchHistoryTab(
                        history = state.searchHistory,
                        isLoading = state.isLoading,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadingHistoryTab(groups: List<HistoryGroup>, isLoading: Boolean, navController: NavController) {
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
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(group.entries, key = { it.id }) { entry ->
                HistoryEntryItem(
                    entry = entry,
                    onClick = {
                        navController.navigate(
                            Routes.reader(bibleAbbr = entry.bibleAbbr, bookId = entry.bookId, chapterId = entry.chapterId)
                        ) {
                            popUpTo(Routes.READER) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun HistoryEntryItem(entry: HistoryEntity, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("h:mm a", LocalLocale.current.platformLocale)
    val verseLabel = entry.verseNumber?.let { ":$it" } ?: ""
    ListItem(
        headlineContent = { Text("${entry.chapterRef}$verseLabel") },
        supportingContent = {
            Text(
                "${entry.bibleName.ifBlank { entry.bibleAbbr.uppercase() }} · ${fmt.format(Date(entry.readAt))}",
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

@Composable
private fun SearchHistoryTab(history: List<com.biblelib.core.database.entities.SearchEntity>, isLoading: Boolean) {
    if (isLoading) {
        return
    }
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
            val fmt = SimpleDateFormat("MMM d, h:mm a", LocalLocale.current.platformLocale)
            ListItem(
                headlineContent = { Text(search.qry) },
                supportingContent = {
                    Text(
                        fmt.format(Date(search.queriedAt)),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Search, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                },
                modifier = Modifier.clickable {}
            )
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}
