package com.biblelib.feature.search.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.biblelib.feature.search.R
import com.biblelib.core.database.entities.SearchEntity

@Composable
fun HistorySection(
    history: List<SearchEntity>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    LazyColumn {
        if (history.isEmpty()) {
            item {
                Box(Modifier
                    .fillMaxWidth()
                    .padding(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.search_history_prompt),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.search_recent_searches), style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.search_clear_history))
                    }
                }
            }
            items(history, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.qry) },
                    leadingContent = {
                        Icon(
                            Icons.Default.History, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier.clickable { onSelect(item.qry) }
                )
            }
        }
    }
}
