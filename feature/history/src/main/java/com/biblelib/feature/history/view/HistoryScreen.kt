package com.biblelib.feature.history.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import androidx.compose.ui.res.stringResource
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.action.ExpandScreenAction
import com.biblelib.core.ui.components.layout.CenteredContentFrame
import com.biblelib.feature.history.R

@OptIn(ExperimentalMaterial3Api::class)
// Shows reading and search history with navigation back to matching content.
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
    onExpand: (() -> Unit)? = null,
    isAdaptiveLayout: Boolean = false,
    onOpenInReader: ((String, String, String, String, String) -> Unit)? = null,
) {
    val state   by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.history_title),
                showGoBack = true,
                closeOnBack = isAdaptiveLayout,
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
                        onOpenInReader = onOpenInReader,
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
