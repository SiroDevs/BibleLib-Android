package com.biblelib.feature.bibles.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.database.entities.BibleEntity

@Composable
fun MultiBibleToggleCard(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Multi-Bible Reader", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Read parallel translations alongside your primary text",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Switch(checked = enabled, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryBiblesCard(
    secondaryBibles: List<String>,
    bibles: List<BibleEntity>,
    downloadProgress: Map<String, Float>,
    onMove: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onDelete: (BibleEntity) -> Unit,
) {
    var reordering by remember { mutableStateOf(false) }
    val showReorderControls = reordering && secondaryBibles.size > 1

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (secondaryBibles.size == 1) "1 secondary bible" else "${secondaryBibles.size} secondary bibles",
                    style = MaterialTheme.typography.labelMedium,
                )
                if (secondaryBibles.size > 1) {
                    TextButton(onClick = { reordering = !reordering }) {
                        Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (reordering) "Done" else "Reorder")
                    }
                }
            }

            secondaryBibles.forEachIndexed { index, abbr ->
                val bible = bibles.find { it.abbreviation == abbr }
                if (bible != null) {
                    SwipeableBibleRow(
                        bible = bible,
                        progress = downloadProgress[abbr],
                        isSecondary = true,
                        onDelete = { onDelete(bible) },
                        onToggleSecondary = { onRemove(abbr) },
                        leadingContent = if (showReorderControls) {
                            {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    IconButton(
                                        onClick = { onMove(abbr, -1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(24.dp),
                                    ) { Icon(Icons.Default.ArrowUpward, "Move up") }
                                    IconButton(
                                        onClick = { onMove(abbr, 1) },
                                        enabled = index < secondaryBibles.size - 1,
                                        modifier = Modifier.size(24.dp),
                                    ) { Icon(Icons.Default.ArrowDownward, "Move down") }
                                }
                            }
                        } else null,
                    )
                    if (index < secondaryBibles.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherBiblesCard(
    bibles: List<BibleEntity>,
    downloadProgress: Map<String, Float>,
    canAddToSecondary: Boolean,
    secondaryCount: Int,
    onAddToSecondary: (String) -> Unit,
    onDelete: (BibleEntity) -> Unit,
) {
    val atCap = secondaryCount >= PrefsRepo.MAX_SECONDARY_BIBLES

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            bibles.forEachIndexed { index, bible ->
                SwipeableBibleRow(
                    bible = bible,
                    progress = downloadProgress[bible.abbreviation],
                    isSecondary = false,
                    onDelete = { onDelete(bible) },
                    onToggleSecondary = if (canAddToSecondary && !atCap) {
                        { onAddToSecondary(bible.abbreviation) }
                    } else null,
                )
                if (index < bibles.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
