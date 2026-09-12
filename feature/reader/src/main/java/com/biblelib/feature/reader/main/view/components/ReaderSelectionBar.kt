package com.biblelib.feature.reader.main.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.biblelib.core.ui.components.share.ShareHelper
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSelectionBar(
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