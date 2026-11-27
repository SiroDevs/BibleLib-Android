package com.biblelib.feature.selection.view.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.design_system.theme.ThemeSelectorDialog
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.selection.view.components.SelectionContinueFab
import com.biblelib.feature.selection.viewmodel.SelectionViewModel
import com.biblelib.feature.selection.R

// Handles Bible selection and its proceed or cancel actions.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    themeRepo: ThemeRepo,
) {
    val uiState by viewModel.uiState.collectAsState()
    val bibles by viewModel.bibles.collectAsState()
    val groupingMode by viewModel.groupingMode.collectAsState()
    val selectedCount by viewModel.selectedCount.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()
    val maxSelections by viewModel.maxSelections.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadStep by viewModel.downloadStep.collectAsState()

    var showThemeDialog by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )

    LaunchedEffect(Unit) {
        viewModel.fetchBibles()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Saved) {
            navController.navigate(Routes.reader()) {
                popUpTo(Routes.SELECTION) {
                    inclusive = true
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            current = themeRepo.selectedTheme,
            onDismiss = {
                showThemeDialog = false
            },
            onThemeSelected = {
                themeRepo.setTheme(it)
                showThemeDialog = false
            }
        )
    }

    val showChrome = uiState !is UiState.Saving && uiState !is UiState.SaveFailed

    Scaffold(
        topBar = {
            if (!showChrome) {
                AppTopBar(title = "BibleLib: Multi-Bible Reader")
            } else {
                AppTopBar(
                    title = "BibleLib: Multi-Bible Reader",
                    tagline = "$selectedCount / $maxSelections bibles selected",
                    actions = {
                        IconButton(
                            onClick = viewModel::fetchBibles
                        ) {
                            Icon(Icons.Default.Refresh, null)
                        }

                        IconButton(
                            onClick = { showThemeDialog = true }
                        ) {
                            Icon(Icons.Default.Brightness6, null)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (uiState is UiState.Loaded) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!viewModel.isFirstInstall) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.cancelReselection()
                                navController.popBackStack()
                            }
                        ) {
                            Icon(Icons.Default.Close, stringResource(R.string.selection_cancel))
                        }
                    }

                    SelectionContinueFab(
                        canProceed = canProceed,
                        onProceed = {
                            if (viewModel.isFirstInstall) {
                                viewModel.saveSelectionAndDownload()
                            } else {
                                viewModel.saveSelectionInBackground()
                            }
                        },
                    )
                }
            }
        }
    ) { padding ->
        SelectionContent(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            bibles = bibles,
            groupingMode = groupingMode,
            selectedCount = selectedCount,
            maxSelections = maxSelections,
            downloadProgress = downloadProgress,
            downloadStep = downloadStep,
            onRetryFetch = viewModel::fetchBibles,
            onContinueDownload = viewModel::continuePrimaryDownload,
            onRestartDownload = viewModel::restartPrimaryDownload,
            onSetGroupingMode = viewModel::setGroupingMode,
            onToggleSelection = viewModel::toggleSelection,
        )
    }
}
