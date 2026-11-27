package com.biblelib.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.feature.settings.view.screen.AppearanceSettingsScreen
import com.biblelib.feature.settings.view.screen.DataSettingsScreen
import com.biblelib.feature.settings.view.screen.ReadingSettingsScreen
import com.biblelib.feature.settings.view.screen.SettingsScreen
import com.biblelib.feature.settings.viewmodel.SettingsViewModel

// Registers settings destinations and supplies their shared dependencies.
fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    themeRepo: ThemeRepo,
    mainViewModel: MainViewModel,
    adaptiveWindowInfo: AdaptiveWindowInfo,
) {
    composable(Routes.SETTINGS) {
        val settingsVm: SettingsViewModel = hiltViewModel()
        SettingsScreen(
            navController = navController,
            adaptiveWindowInfo = adaptiveWindowInfo,
            themeRepo = themeRepo,
            mainViewModel = mainViewModel,
            settingsViewModel = settingsVm,
        )
    }

    composable(Routes.APPEARANCE_SETTINGS) {
        val settingsVm: SettingsViewModel = hiltViewModel()
        AppearanceSettingsScreen(
            navController = navController,
            settViewModel = settingsVm,
            themeRepo = themeRepo,
        )
    }

    composable(Routes.READING_SETTINGS) {
        val settingsVm: SettingsViewModel = hiltViewModel()
        ReadingSettingsScreen(
            navController = navController,
            settViewModel = settingsVm,
        )
    }

    composable(Routes.DATA_SETTINGS) {
        val settingsVm: SettingsViewModel = hiltViewModel()
        DataSettingsScreen(
            navController = navController,
            mainViewModel = mainViewModel,
            settViewModel = settingsVm,
        )
    }
}
