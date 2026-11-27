package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsRowCard
import com.biblelib.feature.settings.viewmodel.ClearTarget
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    settViewModel: SettingsViewModel,
    showGoBack: Boolean = true,
) {
    val pendingClear by settViewModel.pendingClear.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "App Data",
                showGoBack = showGoBack,
                onNavIconClick = { navController.popBackStack() },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SettingsGroupLabel("Manage Your Data") }
            item {
                SettingsRowCard(onClick = { settViewModel.requestClear(ClearTarget.BOOKMARKS) }) {
                    Icon(Icons.Default.Bookmark, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Clear All Bookmarks", modifier = Modifier.weight(1f))
                }
            }
            item {
                SettingsRowCard(onClick = { settViewModel.requestClear(ClearTarget.NOTES) }) {
                    Icon(Icons.Default.StickyNote2, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Clear All Notes", modifier = Modifier.weight(1f))
                }
            }
            item {
                SettingsRowCard(onClick = { settViewModel.requestClear(ClearTarget.HISTORY) }) {
                    Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Clear All History", modifier = Modifier.weight(1f))
                }
            }
            item {
                SettingsRowCard(onClick = { settViewModel.requestClear(ClearTarget.SEARCHES) }) {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Clear Searches", modifier = Modifier.weight(1f))
                }
            }

            item { SettingsGroupLabel("Danger Zone") }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.WarningAmber, null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                "Clear All Data",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Text(
                            "Deletes every downloaded Bible, bookmark, note, history entry, search, and " +
                                "preference — including your theme and reading settings — and restarts the app " +
                                "at Bible selection. This can't be undone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        )
                        androidx.compose.material3.Button(
                            onClick = { settViewModel.requestClear(ClearTarget.ALL_DATA) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.DeleteForever, null)
                            Text("Clear All Data", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }

    if (pendingClear != null) {
        val target = pendingClear!!
        val (title, message) = when (target) {
            ClearTarget.BOOKMARKS -> "Clear all bookmarks?" to
                "This permanently deletes every bookmark across all your Bibles. This can't be undone."
            ClearTarget.NOTES -> "Clear all notes?" to
                "This permanently deletes every note across all your Bibles. This can't be undone."
            ClearTarget.HISTORY -> "Clear all history?" to
                "This permanently deletes your reading history. This can't be undone."
            ClearTarget.SEARCHES -> "Clear search history?" to
                "This permanently deletes your recent searches. This can't be undone."
            ClearTarget.ALL_DATA -> "Clear ALL app data?" to
                "This deletes everything — downloaded Bibles, bookmarks, notes, history, searches, and " +
                    "preferences — and restarts the app at Bible selection. This cannot be undone."
        }
        ConfirmDialog(
            title = title,
            message = message,
            onConfirm = {
                if (target == ClearTarget.ALL_DATA) {
                    settViewModel.confirmClear(mainViewModel)
                } else {
                    settViewModel.confirmClear()
                }
            },
            onDismiss = settViewModel::dismissClear,
        )
    }
}
