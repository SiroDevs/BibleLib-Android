package com.biblelib.feature.bibles.view.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.entities.BibleEntity
import com.biblelib.feature.bibles.view.components.MultiBibleToggleCard
import com.biblelib.feature.bibles.view.components.OtherBiblesCard
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Primary Bible",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        item {
            if (primary != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPrimaryPicker() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = primary.abbreviation.uppercase().take(3),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column {
                            Text(
                                primary.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "${primary.abbreviation.uppercase()} BIBLE • PRIMARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        TextButton(onClick = onOpenPrimaryPicker) {
                            Text("Change")
                        }
                    }
                }
            } else {
                Text("No primary Bible set yet.")
            }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item {
            MultiBibleToggleCard(
                enabled = state.multiBibleEnabled,
                onCheckedChange = onSetMultiBibleEnabled,
            )
        }

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
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Other Bibles",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
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

        item { Spacer(Modifier.height(72.dp)) }
    }
}
