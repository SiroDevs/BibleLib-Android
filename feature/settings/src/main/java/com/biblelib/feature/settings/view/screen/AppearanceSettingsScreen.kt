package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.data.repos.ThemeMode
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.data.repos.appThemeName
import com.biblelib.core.design_system.customization.AppReaderBackgrounds
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.settings.view.components.SettingsDropdownRow
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    settViewModel: SettingsViewModel,
    themeRepo: ThemeRepo,
    showGoBack: Boolean = true,
) {
    var themeExpanded by remember { mutableStateOf(false) }
    val readerBackgroundId by settViewModel.readerBackgroundId.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Appearance",
                showGoBack = showGoBack,
                onNavIconClick = { navController.popBackStack() },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SettingsGroupLabel("Display") }
            item {
                SettingsDropdownRow(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    currentLabel = appThemeName(themeRepo.selectedTheme),
                    expanded = themeExpanded,
                    onToggle = { themeExpanded = !themeExpanded },
                    options = ThemeMode.entries.map { it to appThemeName(it) },
                    selected = themeRepo.selectedTheme,
                    onSelect = {
                        themeRepo.setTheme(it)
                        themeExpanded = false
                    },
                )
            }

            item {
                Text(
                    "Reader Background",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        maxItemsInEachRow = 5,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppReaderBackgrounds.ALL.forEach { bg ->
                            val isSelected = bg.id == readerBackgroundId
                            val swatchLuminance = (0.299f * bg.swatch.red + 0.587f * bg.swatch.green + 0.114f * bg.swatch.blue)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { settViewModel.setReaderBackground(bg.id) },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .fillMaxWidth()
                                        .background(bg.swatch, CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check, null,
                                            tint = if (swatchLuminance > 0.5f) Color.Black else Color.White,
                                        )
                                    }
                                }
                                Text(
                                    bg.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
