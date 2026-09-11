package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.share.ShareHelper
import com.biblelib.feature.reader.R
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    navController: NavController,
    state: ReaderUiState,
    onBibleClick: () -> Unit,
    onBookClick: () -> Unit,
    bookSwitchEnabled: Boolean = true,
    onBookSwitchBlocked: () -> Unit = {},
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBibleClick() }
                        .padding(horizontal = 5.dp)
                ) {
                    Text(
                        text = "${state.activeBibleAbbr.uppercase().take(3)}: ${state.activeBible}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (bookSwitchEnabled) 1f else 0.5f)
                        .clickable { if (bookSwitchEnabled) onBookClick() else onBookSwitchBlocked() }
                        .padding(horizontal = 5.dp)
                ) {
                    Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(25.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = state.activeChapter?.reference ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(22.dp))
                }
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                Icon(Icons.Default.Search, "Search")
            }
            IconButton(onClick = { showMoreMenu = true }) {
                Icon(Icons.Default.MoreVert, "More")
            }
            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Cast to PC/Devices") },
                    leadingIcon = { Icon(Icons.Default.Cast, null) },
                    onClick = {
                        showMoreMenu = false
                        navController.navigate(Routes.CASTING)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Scripture Lists") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) },
                    onClick = {
                        showMoreMenu = false
                        navController.navigate(Routes.SCRIPTURE_LISTS)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Bookmarks & Notes") },
                    leadingIcon = { Icon(Icons.Default.Bookmarks, null) },
                    onClick = {
                        showMoreMenu = false
                        navController.navigate(Routes.BOOKMARKS_NOTES)
                    },
                )
                DropdownMenuItem(
                    text = { Text("View History") },
                    leadingIcon = { Icon(Icons.Default.History, null) },
                    onClick = {
                        showMoreMenu = false
                        navController.navigate(Routes.HISTORY)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Manage Settings") },
                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                    onClick = {
                        showMoreMenu = false
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "AppIcon",
                modifier = Modifier.size(50.dp),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSelectionTopBar(
    selectedCount: Int,
    viewModel: ReaderViewModel,
) {
    val context = LocalContext.current

    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = viewModel::clearSelection) {
                Icon(Icons.Default.Close, "Cancel selection")
            }
        },
        actions = {
            IconButton(onClick = viewModel::openColorPicker) {
                Icon(Icons.Default.Bookmark, "Bookmark")
            }
            IconButton(
                onClick = viewModel::openNotesForSelection,
                enabled = selectedCount == 1
            ) {
                Icon(Icons.Default.EditNote, "Notes")
            }
            IconButton(
                onClick = {
                    viewModel.buildSelectionShareText()?.let { ShareHelper.copyText(context, it) }
                },
            ) {
                Icon(Icons.Default.ContentCopy, "Copy")
            }
            IconButton(
                onClick = {
                    viewModel.buildSelectionShareText()?.let { ShareHelper.shareText(context, it) }
                },
            ) {
                Icon(Icons.Default.Share, "Share")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}
