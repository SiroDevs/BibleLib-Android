package com.biblelib.feature.selection.view.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.design_system.theme.ThemeSelectorDialog
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.view.components.BibleListItem
import com.biblelib.feature.selection.view.components.BibleSavingProgress
import com.biblelib.feature.selection.view.components.DownloadFailedState
import com.biblelib.feature.selection.view.components.FilterChipStrip
import com.biblelib.feature.selection.view.components.GroupHeader
import com.biblelib.feature.selection.view.components.GroupingFilmStrip
import com.biblelib.feature.selection.view.components.ProceedBar
import com.biblelib.feature.selection.utils.GridEntry
import com.biblelib.feature.selection.viewmodel.SelectionViewModel
import com.biblelib.feature.selection.utils.buildGridEntries

private const val GRID_COLUMNS = 2

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

    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }

    val countryFilters = remember { mutableStateMapOf<String, String>() }

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
        bottomBar = {
            if (uiState is UiState.Loaded) {
                ProceedBar(
                    canProceed = canProceed,
                    onProceed = viewModel::saveSelectionAndDownload
                )
            }
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (val state = uiState) {
                UiState.Loading -> BibleCardShimmer()

                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = viewModel::fetchBibles
                )

                UiState.Saving -> {
                    val animatedProgress by animateFloatAsState(
                        targetValue = downloadProgress,
                        label = "download_progress",
                    )
                    BibleSavingProgress(progress = animatedProgress, step = downloadStep)
                }

                is UiState.SaveFailed -> {
                    DownloadFailedState(
                        message = state.message,
                        progress = state.progress,
                        onContinue = viewModel::continuePrimaryDownload,
                        onRestart = viewModel::restartPrimaryDownload,
                    )
                }

                else -> {
                    val entries = remember(
                        bibles,
                        groupingMode,
                        expandedGroups.toMap(),
                        countryFilters.toMap(),
                    ) {
                        buildGridEntries(
                            bibles = bibles,
                            mode = groupingMode,
                            expandedGroups = expandedGroups,
                            countryFilters = countryFilters,
                        )
                    }

                    Column(Modifier.fillMaxSize()) {
                        GroupingFilmStrip(
                            selected = groupingMode,
                            onSelected = viewModel::setGroupingMode,
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(GRID_COLUMNS),
                            contentPadding = PaddingValues(all = 2.dp),
                        ) {
                            items(
                                items = entries,
                                key = { entry ->
                                    when (entry) {
                                        is GridEntry.Header -> "header_${entry.key}"
                                        is GridEntry.CountryFilterStrip -> "filter_${entry.key}"
                                        is GridEntry.Item -> "item_${entry.key}"
                                    }
                                },
                                span = { entry ->
                                    when (entry) {
                                        is GridEntry.Header -> GridItemSpan(maxLineSpan)
                                        is GridEntry.CountryFilterStrip -> GridItemSpan(maxLineSpan)
                                        is GridEntry.Item -> if (entry.soloInGroup) {
                                            GridItemSpan(maxLineSpan)
                                        } else {
                                            GridItemSpan(1)
                                        }
                                    }
                                },
                            ) { entry ->
                                when (entry) {
                                    is GridEntry.Header -> {
                                        val isExpanded = expandedGroups[entry.key] ?: true
                                        GroupHeader(
                                            title = entry.title,
                                            totalInGroup = entry.totalCount,
                                            expanded = isExpanded,
                                            onClick = {
                                                expandedGroups[entry.key] = !isExpanded
                                            },
                                        )
                                    }

                                    is GridEntry.CountryFilterStrip -> {
                                        FilterChipStrip(
                                            options = entry.options,
                                            selected = entry.selected,
                                            onSelected = { country ->
                                                countryFilters[entry.continentKey] = country
                                            },
                                        )
                                    }

                                    is GridEntry.Item -> {
                                        val item = entry.bible
                                        Box(Modifier.padding(2.dp)) {
                                            BibleListItem(
                                                name = item.data.name,
                                                description = item.data.description,
                                                abbreviation = item.data.abbreviation,
                                                language = item.data.language.name,
                                                isSelected = item.isSelected,
                                                isDisabled =
                                                    !item.isSelected &&
                                                            selectedCount >= maxSelections,
                                                onClick = {
                                                    viewModel.toggleSelection(item.data.abbreviation)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
