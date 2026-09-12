package com.biblelib.feature.reader.home.view.components.actions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.feature.reader.home.utils.ReaderUiState
import kotlinx.coroutines.launch

@Composable
fun ReaderFab(
    state: ReaderUiState,
    navController: NavController,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val isAtTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    val showScrollToTop by remember {
        derivedStateOf { !isAtTop }
    }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Rudi Juu")
            }
        }

        ExtendedFloatingActionButton(
            onClick = { navController.navigate(Routes.scriptureOpener(state.activeBibleAbbr, state.activeBible)) },
            expanded = isAtTop,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            icon = { Icon(Icons.Filled.ManageSearch, "Scripture Opener") },
            text = { Text("Scripture Opener") },
        )
    }
}

@Composable
fun SpeedButtonsFab(
    visible: Boolean,
    onSpeedUp: () -> Unit,
    onSpeedDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IconButton(onClick = onSpeedUp) {
                    Icon(Icons.Default.Add, contentDescription = "Speed up auto scroll")
                }

                IconButton(onClick = onSpeedDown) {
                    Icon(Icons.Default.Remove, contentDescription = "Slow down auto scroll")
                }
            }
        }
    }
}
