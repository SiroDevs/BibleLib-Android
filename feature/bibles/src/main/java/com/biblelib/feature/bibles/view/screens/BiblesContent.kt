package com.biblelib.feature.bibles.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.core.ui.components.general.SectionHeaderRow
import com.biblelib.feature.bibles.view.components.MultiBibleToggleCard
import com.biblelib.feature.bibles.view.components.OtherBiblesCard
import com.biblelib.feature.bibles.view.components.PrimaryBibleCard
import com.biblelib.feature.bibles.view.components.SecondaryBiblesCard
import com.biblelib.feature.bibles.viewmodel.BiblesUiState

@Composable
fun BiblesContent(
    state: BiblesUiState,
    onOpenPrimaryPicker: () -> Unit,
    onSetMultiBibleEnabled: (Boolean) -> Unit,
    onMoveSecondary: (String, Int) -> Unit,
    onRemoveSecondary: (String) -> Unit,
    onAddToSecondary: (String) -> Unit,
    onRequestDelete: (BibleEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = state.bibles.find { it.abbreviation == state.primaryAbbr }

    val otherBibles = if (state.multiBibleEnabled) {
        state.bibles.filter { it.abbreviation != state.primaryAbbr && it.abbreviation !in state.secondaryBibles }
    } else {
        state.bibles.filter { it.abbreviation != state.primaryAbbr }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionHeaderRow(
                leftText = "Primary Bible",
                rightText = "Tap to Change",
                contentPadding = PaddingValues(horizontal = 5.dp),
            )
        }
        item {
            PrimaryBibleCard(primary = primary, onClick = onOpenPrimaryPicker)
        }

        item { Spacer(Modifier.height(1.dp)) }
        item {
            MultiBibleToggleCard(
                enabled = state.multiBibleEnabled,
                onCheckedChange = onSetMultiBibleEnabled,
            )
        }

        item { Spacer(Modifier.height(1.dp)) }
        if (state.multiBibleEnabled) {
            item {
                SecondaryBiblesCard(
                    secondaryBibles = state.secondaryBibles,
                    bibles = state.bibles,
                    downloadProgress = state.downloadProgress,
                    onMove = onMoveSecondary,
                    onRemove = onRemoveSecondary,
                    onDelete = onRequestDelete,
                )
            }
        }

        if (otherBibles.isNotEmpty()) {
            item { Spacer(Modifier.height(1.dp)) }
            item {
                OtherBiblesCard(
                    bibles = otherBibles,
                    downloadProgress = state.downloadProgress,
                    canAddToSecondary = state.multiBibleEnabled,
                    secondaryCount = state.secondaryBibles.size,
                    onAddToSecondary = onAddToSecondary,
                    onDelete = onRequestDelete,
                )
            }
        }

        item { Spacer(Modifier.height(50.dp)) }
    }
}
