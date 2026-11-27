package com.biblelib.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biblelib.R
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.core.ui.utils.rememberAdaptiveWindowInfo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.navigation.graphs.mainGraph
import com.biblelib.navigation.graphs.miscGraph
import com.biblelib.navigation.graphs.searchGraph
import com.biblelib.navigation.graphs.settingsGraph

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
// Routes app destinations and adds navigation rail space on wider windows.
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    themeRepo: ThemeRepo,
    prefsRepo: PrefsRepo,
    mainViewModel: MainViewModel,
) {
    val isReady by mainViewModel.isReady.collectAsState()
    val destination by mainViewModel.destination.collectAsState()
    val adaptiveWindowInfo = rememberAdaptiveWindowInfo()

    if (!isReady) return

    val startDestination = when (destination) {
        is MainViewModel.Destination.Selection -> Routes.SELECTION
        is MainViewModel.Destination.Reader -> Routes.READER
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showNavigationRail = adaptiveWindowInfo.widthClass != AdaptiveWidthClass.COMPACT &&
            currentDestination?.route != Routes.SELECTION

    Row(Modifier.fillMaxSize()) {
        if (showNavigationRail) {
            PrimaryNavigationRail(
                navController = navController,
                currentDestination = currentDestination,
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f),
        ) {
            mainGraph(
                navController = navController,
                themeRepo = themeRepo,
                prefsRepo = prefsRepo,
                mainViewModel = mainViewModel,
                adaptiveWindowInfo = adaptiveWindowInfo,
            )
            settingsGraph(
                navController = navController,
                themeRepo = themeRepo,
                mainViewModel = mainViewModel,
            )
            searchGraph(navController = navController)
            miscGraph(
                navController = navController,
                prefsRepo = prefsRepo
            )
        }
    }
}

@Composable
// Provides top-level destinations on medium and wider windows.
private fun PrimaryNavigationRail(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination?,
) {
    val destinations = listOf(
        PrimaryDestination(
            Routes.reader(), Routes.READER, Icons.AutoMirrored.Filled.MenuBook, R.string.navigation_read,
        ),
        PrimaryDestination(
            Routes.SEARCH, Routes.SEARCH, Icons.Default.Search, R.string.navigation_search,
        ),
        PrimaryDestination(
            Routes.HISTORY, Routes.HISTORY, Icons.Default.History, R.string.navigation_history,
        ),
        PrimaryDestination(
            Routes.BOOKMARKS_NOTES,
            Routes.BOOKMARKS_NOTES,
            Icons.Default.Bookmarks,
            R.string.navigation_bookmarks,
        ),
        PrimaryDestination(
            Routes.SETTINGS, Routes.SETTINGS, Icons.Default.Settings,
            R.string.navigation_settings,
        ),
    )
    val activeRoute = when (currentDestination?.route) {
        Routes.SEARCH, Routes.SCRIPTURE_OPENER -> Routes.SEARCH

        Routes.HISTORY -> Routes.HISTORY
        Routes.BOOKMARKS_NOTES -> Routes.BOOKMARKS_NOTES
        Routes.SETTINGS, Routes.APPEARANCE_SETTINGS, Routes.READING_SETTINGS,
        Routes.DATA_SETTINGS -> Routes.SETTINGS

        else -> Routes.READER
    }

    NavigationRail {
        destinations.forEach { destination ->
            NavigationRailItem(
                selected = destination.selectedRoute == activeRoute,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(Routes.READER) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { androidx.compose.material3.Text(stringResource(destination.label)) },
                alwaysShowLabel = true,
            )
        }
    }
}

// Describes one entry in the app's primary navigation rail.
private data class PrimaryDestination(
    val route: String,
    val selectedRoute: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: Int,
)
