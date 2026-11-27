package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.feature.settings.R
import com.biblelib.feature.settings.view.components.AlsoBySiroApps
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsNavRow
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

// Displays settings navigation beside the selected settings detail.
@Composable
fun SettingsSplitPane(
    navController: NavController,
    adaptiveWindowInfo: AdaptiveWindowInfo,
    themeRepo: ThemeRepo,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
) {
    var selectedDetail by rememberSaveable { mutableStateOf(Routes.APPEARANCE_SETTINGS) }
    val hasHinge = adaptiveWindowInfo.hasVerticalSeparatingHinge

    Row(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(if (hasHinge) 1f else 0.42f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SettingsGroupLabel(stringResource(R.string.settings_preferences_group)) }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_appearance_summary),
                    selected = selectedDetail == Routes.APPEARANCE_SETTINGS,
                    onClick = { selectedDetail = Routes.APPEARANCE_SETTINGS },
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.settings_reading),
                    subtitle = stringResource(R.string.settings_reading_summary),
                    selected = selectedDetail == Routes.READING_SETTINGS,
                    onClick = { selectedDetail = Routes.READING_SETTINGS },
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.DataUsage,
                    title = stringResource(R.string.settings_reset_app_data),
                    selected = selectedDetail == Routes.DATA_SETTINGS,
                    onClick = { selectedDetail = Routes.DATA_SETTINGS },
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

        if (hasHinge) {
            Spacer(Modifier.width(adaptiveWindowInfo.verticalHingeGutter))
        } else {
            VerticalDivider(Modifier.fillMaxHeight().padding(vertical = 8.dp))
        }

        Box(Modifier.weight(if (hasHinge) 1f else 0.58f).fillMaxHeight()) {
            when (selectedDetail) {
                Routes.READING_SETTINGS -> ReadingSettingsScreen(
                    navController = navController,
                    settViewModel = settingsViewModel,
                    showGoBack = false,
                )
                Routes.DATA_SETTINGS -> DataSettingsScreen(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    settViewModel = settingsViewModel,
                    showGoBack = false,
                )
                else -> AppearanceSettingsScreen(
                    navController = navController,
                    settViewModel = settingsViewModel,
                    themeRepo = themeRepo,
                    showGoBack = false,
                )
            }
        }
    }
}
