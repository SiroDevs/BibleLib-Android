package com.biblelib.feature.search.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.layout.CenteredContentFrame
import com.biblelib.feature.search.viewmodel.SearchViewModel
import com.biblelib.feature.search.R
import com.biblelib.feature.search.view.components.BibleFilterStrip
import com.biblelib.feature.search.view.components.HistorySection
import com.biblelib.feature.search.view.components.SearchResultItem

@OptIn(ExperimentalMaterial3Api::class)
// Searches Bible text and opens matching verses in the reader.
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel,
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val bibles by viewModel.bibles.collectAsState()
    val selectedBibleAbbr by viewModel.selectedBibleAbbr.collectAsState()
    val bookNames by viewModel.bookNames.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.search_back),
                            )
                        }
                    },
                    title = {
                        SearchField(
                            query = query,
                            onChange = viewModel::onQueryChange,
                            onClear = viewModel::clearQuery,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                BibleFilterStrip(
                    bibles = bibles,
                    selectedAbbr = selectedBibleAbbr,
                    onSelect = viewModel::selectBible,
                )
            }
        }
    ) { padding ->
        CenteredContentFrame(Modifier.fillMaxSize().padding(padding)) {
            when {
                isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                query.length >= 3 && results.isEmpty() && !isSearching -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.search_no_results, query),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                query.length >= 3 -> LazyColumn {
                    item {
                        Text(
                            stringResource(R.string.search_results_count, results.size),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    items(results, key = { it.verseId }) { verse ->
                        SearchResultItem(
                            verse = verse,
                            query = query,
                            bookName = bookNames[verse.bookId] ?: verse.bookId,
                            onClick = { tapped ->
                                navController.navigate(
                                    Routes.reader(
                                        bibleAbbr = selectedBibleAbbr,
                                        bookId = tapped.bookId,
                                        chapterId = tapped.chapterId,
                                        verseId = tapped.verseId,
                                        searchQuery = query,
                                    )
                                ) {
                                    popUpTo(Routes.READER) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                else -> HistorySection(
                    history = searchHistory,
                    onSelect = viewModel::searchFromHistory,
                    onClear = viewModel::clearSearchHistory,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(R.string.search_clear),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
