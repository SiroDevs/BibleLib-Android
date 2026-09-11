package com.biblelib.feature.bibles.view.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.general.ConfirmDialog
import com.biblelib.core.ui.components.general.InfoDialog
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.feature.bibles.view.components.PrimaryBiblePickerDialog
import com.biblelib.feature.bibles.viewmodel.BiblesViewModel

private const val BIBLES_MANAGEMENT_INFO = "You can add a Bible to the Multi-Bible Reader " +
    "by swiping it to the right from \"Other Bibles\" — swipe it right again to remove it as " +
    "a secondary translation.\n\n" +
    "To delete a Bible from your device entirely, swipe it to the left and confirm.\n\n" +
    "Want more Bibles than what's shown here? Tap \"Change Selection\" below to go back to " +
    "the Bible picker and add (or remove) translations from your library."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiblesScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: BiblesViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Bibles",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = viewModel::openManagementInfo) {
                        Icon(Icons.Default.Info, "How bible management works")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Change Selection") },
                icon = { Icon(Icons.Default.SwapHoriz, null) },
                onClick = {
                    viewModel.requestReselection()
                    navController.navigate(Routes.SELECTION) {
                        launchSingleTop = true
                    }
                },
            )
        }
    ) { padding ->
        BiblesContent(
            state = state,
            modifier = Modifier.padding(padding),
            onOpenPrimaryPicker = viewModel::openPrimaryPicker,
            onSetMultiBibleEnabled = viewModel::setMultiBibleEnabled,
            onMoveSecondary = viewModel::moveSecondaryBible,
            onRemoveSecondary = viewModel::toggleSecondaryBible,
            onAddToSecondary = viewModel::toggleSecondaryBible,
            onRequestDelete = viewModel::requestDelete,
        )
    }

    if (state.showPrimaryPicker) {
        PrimaryBiblePickerDialog(
            bibles = state.bibles,
            current = state.primaryAbbr,
            onDismiss = viewModel::dismissPrimaryPicker,
            onSelect = viewModel::setPrimaryBible,
        )
    }

    state.pendingDelete?.let { bible ->
        ConfirmDialog(
            title = "Remove ${bible.name}?",
            message = "This deletes all downloaded content for this Bible from your device. This can't be undone.",
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    if (state.showFirstOpenPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFirstOpenPrompt(showInfoNext = false) },
            title = { Text("Would you like to know how to manage your bibles?") },
            text = {
                Text(
                    "Learn how to add or remove Bibles from the Multi-Bible Reader, and how " +
                        "to add more translations to your library."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissFirstOpenPrompt(showInfoNext = true) }) {
                    Text("Show me")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissFirstOpenPrompt(showInfoNext = false) }) {
                    Text("No thanks")
                }
            },
        )
    }

    if (state.showManagementInfo) {
        InfoDialog(
            title = "Managing your Bibles",
            message = BIBLES_MANAGEMENT_INFO,
            onDismiss = viewModel::dismissManagementInfo,
        )
    }
}
