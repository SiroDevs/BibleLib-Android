package com.biblelib.feature.reader.home.view.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.feature.reader.home.utils.ReaderUiState
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSelectorSheet(
    state: ReaderUiState,
    onSelect: (String) -> Unit,
    onOpenBibles: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Switch Your Primary Bible",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TextButton(
                onClick = onOpenBibles,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(
                    Icons.Default.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Manage Bibles")
            }
        }

        state.savedBibles.filter { it.isDownloaded }.forEach { bible ->
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF3A1300)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = bible.abbreviation.uppercase().take(3),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = Color(0xFFFFFFFF),
                            )
                        }
                    }
                },
                headlineContent = { Text(bible.name) },
                supportingContent = {
                    Text(
                        text = "${bible.languageName.uppercase()} ~ ${
                            bible.description}",
                        maxLines = 1,
                    )
                                    },
                trailingContent = {
                    when {
                        bible.abbreviation == state.activeBibleAbbr -> Icon(
                            Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable(enabled = bible.isDownloaded) {
                    onSelect(bible.abbreviation)
                },
            )
            HorizontalDivider(thickness = 0.5.dp)
        }

        Spacer(Modifier.height(24.dp))
    }
}
