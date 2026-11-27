package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.core.ui.components.layout.CenteredContentFrame
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.feature.settings.view.components.AlsoBySiroApps
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsNavRow
import com.biblelib.feature.settings.viewmodel.SettingsViewModel
import com.biblelib.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
// Lists app settings and related BibleLib destinations.
@Composable
fun SettingsScreen(
    navController: NavController,
    adaptiveWindowInfo: AdaptiveWindowInfo,
    themeRepo: ThemeRepo,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    additionalDetailContent: @Composable (String) -> Unit = {},
) {
    val isAdaptive = adaptiveWindowInfo.widthClass != AdaptiveWidthClass.COMPACT
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() },
                actions = {
                    if (!isAdaptive) {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.settings_more))
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_reset_app_data)) },
                                leadingIcon = { Icon(Icons.Default.DataUsage, null) },
                                onClick = {
                                    showMoreMenu = false
                                    navController.navigate(Routes.DATA_SETTINGS)
                                },
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isAdaptive) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                SettingsSplitPane(
                    navController = navController,
                    adaptiveWindowInfo = adaptiveWindowInfo,
                    themeRepo = themeRepo,
                    mainViewModel = mainViewModel,
                    settingsViewModel = settingsViewModel,
                    additionalDetailContent = additionalDetailContent,
                )
            }
        } else CenteredContentFrame(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { SettingsGroupLabel(stringResource(R.string.settings_preferences_group)) }
                item {
                    SettingsNavRow(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_appearance),
                        subtitle = stringResource(R.string.settings_appearance_summary),
                        onClick = { navController.navigate(Routes.APPEARANCE_SETTINGS) },
                    )
                }
                item {
                    SettingsNavRow(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = stringResource(R.string.settings_reading),
                        subtitle = stringResource(R.string.settings_reading_summary),
                        onClick = { navController.navigate(Routes.READING_SETTINGS) },
                    )
                }

                item { SettingsGroupLabel(stringResource(R.string.settings_bibles_group)) }
                item {
                    SettingsNavRow(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_manage_bibles),
                        subtitle = stringResource(R.string.settings_manage_bibles_summary),
                        onClick = { navController.navigate(Routes.BIBLES) },
                    )
                }

                item { SettingsGroupLabel(stringResource(R.string.settings_support_group)) }
                item {
                    SettingsNavRow(
                        icon = Icons.Default.MonetizationOn,
                        title = stringResource(R.string.settings_donate),
                        subtitle = stringResource(R.string.settings_donate_summary),
                        onClick = { navController.navigate(Routes.DONATION) },
                    )
                }
                item {
                    SettingsNavRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = stringResource(R.string.settings_help),
                        subtitle = stringResource(R.string.settings_help_summary),
                        onClick = { navController.navigate(Routes.HELP) },
                    )
                }

                item { SettingsGroupLabel(stringResource(R.string.settings_user_manual_group)) }
                item {
                    SettingsNavRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_how_it_works),
                        subtitle = stringResource(R.string.settings_how_it_works_summary),
                        onClick = { navController.navigate(Routes.HOW_IT_WORKS) },
                    )
                }

                item { SettingsGroupLabel(stringResource(R.string.settings_also_by)) }
                item { AlsoBySiroApps() }
            }
        }
    }
}
