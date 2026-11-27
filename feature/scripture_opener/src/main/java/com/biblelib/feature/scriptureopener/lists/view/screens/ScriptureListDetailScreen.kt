package com.biblelib.feature.scripture_opener.lists.view.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.database.entities.ScriptureItemEntity
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.action.ExpandScreenAction
import com.biblelib.core.ui.components.general.QuickFormDialog
import com.biblelib.feature.scripture_opener.lists.viewmodel.ScriptureListDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureListDetailScreen(
    navController: NavHostController,
    viewModel: ScriptureListDetailViewModel,
    listId: Long,
    onExpand: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    var isRenaming by remember { mutableStateOf(false) }

    LaunchedEffect(listId) { viewModel.initialize(listId) }

    LaunchedEffect(state.navigateToReader) {
        val target = state.navigateToReader ?: return@LaunchedEffect
        navController.navigate(Routes.reader(target.bibleAbbr, target.bookId, target.chapterId))
        viewModel.consumeNavigation()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.listName.ifBlank { "Scripture List" },
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    onExpand?.let { ExpandScreenAction(it) }
                    if (!state.isLoading && state.items.isNotEmpty()) {
                        IconButton(onClick = { isRenaming = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename list")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!state.isLoading && state.items.isNotEmpty()) {
                FloatingActionButton(onClick = { viewModel.play() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Open in reader")
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Something went wrong.", style = MaterialTheme.typography.bodyLarge)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ScriptureItemRow(item)
                    }
                }
            }
        }
    }

    if (isRenaming) {
        QuickFormDialog(
            title = "Rename list",
            label = "List name",
            initialValue = state.listName,
            onDismiss = { isRenaming = false },
            onConfirm = { newName ->
                viewModel.rename(newName)
                isRenaming = false
            },
        )
    }
}

@Composable
private fun ScriptureItemRow(item: ScriptureItemEntity) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = item.reference,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.bibleName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
