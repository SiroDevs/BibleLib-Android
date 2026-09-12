package com.biblelib.feature.reader.main.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.feature.reader.main.utils.ReaderUiState
import com.biblelib.feature.reader.main.viewmodel.ReaderViewModel
import androidx.core.graphics.toColorInt

@Composable
fun HighlightColorPickerDialog(viewModel: ReaderViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissColorPicker,
        title = { Text("Choose a highlight color") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ReaderViewModel.HIGHLIGHT_COLORS.forEach { hex ->
                    val color = runCatching { Color(hex.toColorInt()) }
                        .getOrDefault(Color.Gray)
                    Column(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { viewModel.chooseHighlightColor(hex) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::dismissColorPicker) { Text("Cancel") }
        },
    )
}

@Composable
fun BookmarkOptionsDialog(viewModel: ReaderViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::cancelPendingHighlight,
        title = { Text("Highlight applied") },
        text = { Text("Would you like to just bookmark these verses, or also add a note?") },
        confirmButton = {
            TextButton(onClick = { viewModel.confirmBookmarkWithNotes() }) {
                Text("Bookmark with notes")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::confirmBookmarkOnly) { Text("Bookmark only") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsDialog(
    state: ReaderUiState,
    themeRepo: ThemeRepo,
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Quick Settings")
        },
        text = {
            Column {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeRepo.selectedTheme == mode,
                            onClick = { themeRepo.setTheme(mode) },
                            label = {
                                Text(
                                    mode.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Font size: ${state.fontSizeSp.toInt()}sp",
                    style = MaterialTheme.typography.labelMedium,
                )

                Slider(
                    value = state.fontSizeSp,
                    onValueChange = viewModel::setFontSize,
                    valueRange = AppFonts.MIN_FONT_SP..AppFonts.MAX_FONT_SP,
                    steps = ((AppFonts.MAX_FONT_SP - AppFonts.MIN_FONT_SP) / 2).toInt() - 1,
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Multi-Bible Reader",
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Switch(
                        checked = state.multiBibleReaderEnabled,
                        onCheckedChange = viewModel::setMultiBibleReaderEnabled,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}
