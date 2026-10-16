package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.settings.view.components.AlsoBySiroApps
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsNavRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "BibleLib: Multi-Bible Reader",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                    DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Reset App Data") },
                            leadingIcon = { Icon(Icons.Default.DataUsage, null) },
                            onClick = {
                                showMoreMenu = false
                                navController.navigate(Routes.DATA_SETTINGS)
                            },
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SettingsGroupLabel("Preferences") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Theme, reader background",
                    onClick = { navController.navigate(Routes.APPEARANCE_SETTINGS) },
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.MenuBook,
                    title = "Reading",
                    subtitle = "Font, size",
                    onClick = { navController.navigate(Routes.READING_SETTINGS) },
                )
            }

            item { SettingsGroupLabel("BIBLES") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Palette,
                    title = "Manage Bibles",
                    subtitle = "Primary and Secondary Bibles",
                    onClick = { navController.navigate(Routes.BIBLES) },
                )
            }

            item { SettingsGroupLabel("SUPPORT SECTION") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.MonetizationOn,
                    title = "Donate to BibleLib",
                    subtitle = "BibleLib appreciates your support",
                    onClick = { navController.navigate(Routes.DONATION) },
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Submit a Complaint or Compliment",
                    onClick = { navController.navigate(Routes.HELP) },
                )
            }

            item { SettingsGroupLabel("USER MANUAL") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Info,
                    title = "Home it Works",
                    subtitle = "Learn about the features of BibleLib ",
                    onClick = { navController.navigate(Routes.HOW_IT_WORKS) },
                )
            }

            item { SettingsGroupLabel("Also by Siro Devs") }
            item { AlsoBySiroApps() }
        }
    }
}
