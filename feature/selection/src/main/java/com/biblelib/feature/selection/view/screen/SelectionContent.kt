package com.biblelib.feature.selection.view.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.common.entity.Selectable
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.network.dtos.BibleInfoDto
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.utils.GridEntry
import com.biblelib.feature.selection.utils.GroupingMode
import com.biblelib.feature.selection.utils.buildGridEntries
import com.biblelib.feature.selection.view.components.BibleListItem
import com.biblelib.feature.selection.view.components.BibleSavingProgress
import com.biblelib.feature.selection.view.components.DownloadFailedState
import com.biblelib.feature.selection.view.components.FilterChipStrip
import com.biblelib.feature.selection.view.components.GroupHeader
import com.biblelib.feature.selection.view.components.GroupingFilmStrip

private const val GRID_COLUMNS = 2

@Composable
fun SelectionContent(
    uiState: UiState,
    bibles: List<Selectable<BibleInfoDto>>,
    groupingMode: GroupingMode,
    selectedCount: Int,
    maxSelections: Int,
    downloadProgress: Float,
    downloadStep: String,
    onRetryFetch: () -> Unit,
    onContinueDownload: () -> Unit,
    onRestartDownload: () -> Unit,
    onSetGroupingMode: (GroupingMode) -> Unit,
    onToggleSelection: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>() }
    val countryFilters = remember { mutableStateMapOf<String, String>() }

    Box(modifier.fillMaxSize()) {
        when (val state = uiState) {
            UiState.Loading -> BibleCardShimmer()

            is UiState.Error -> ErrorState(
                message = state.message,
                onRetry = onRetryFetch,
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
                    onContinue = onContinueDownload,
                    onRestart = onRestartDownload,
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
                        onSelected = onSetGroupingMode,
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
                                                onToggleSelection(item.data.abbreviation)
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
