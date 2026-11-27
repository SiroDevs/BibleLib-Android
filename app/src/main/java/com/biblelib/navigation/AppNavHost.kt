package com.biblelib.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    var expandedRoute by rememberSaveable { mutableStateOf<String?>(null) }

    if (!isReady) return

    val startDestination = when (destination) {
        is MainViewModel.Destination.Selection -> Routes.SELECTION
        is MainViewModel.Destination.Reader -> Routes.READER
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    LaunchedEffect(currentRoute) { expandedRoute = null }
    val onExpandCurrentScreen: () -> Unit = {
        navController.currentDestination?.route?.let { expandedRoute = it }
    }
    val isSettingsRoute = currentRoute in setOf(
        Routes.SETTINGS, Routes.APPEARANCE_SETTINGS, Routes.READING_SETTINGS, Routes.DATA_SETTINGS,
    )
    val splitReaderRoute = currentRoute in setOf(
        Routes.SEARCH, Routes.SCRIPTURE_OPENER, Routes.SCRIPTURE_LISTS, Routes.SCRIPTURE_LIST_DETAIL,
        Routes.HISTORY, Routes.BOOKMARKS_NOTES,
    )
    val isExpanded = currentRoute != null && expandedRoute == currentRoute
    val showNavigationRail = adaptiveWindowInfo.widthClass != AdaptiveWidthClass.COMPACT &&
            currentDestination?.route != Routes.SELECTION && !isSettingsRoute && !isExpanded
    val showReaderPane = adaptiveWindowInfo.widthClass != AdaptiveWidthClass.COMPACT &&
            splitReaderRoute && !isExpanded

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val railWidth = if (showNavigationRail) 80.dp else 0.dp
        val paneGap = when {
            !showReaderPane -> 0.dp
            adaptiveWindowInfo.hasVerticalSeparatingHinge -> adaptiveWindowInfo.verticalHingeGutter
            else -> 1.dp
        }
        val availablePaneWidth = (maxWidth - railWidth - paneGap).coerceAtLeast(1.dp)
        val leftPaneWidth = if (adaptiveWindowInfo.hasVerticalSeparatingHinge) {
            (maxWidth / 2 - railWidth - paneGap / 2).coerceIn(1.dp, availablePaneWidth - 1.dp)
        } else availablePaneWidth / 2
        val leftPaneWeight = leftPaneWidth / availablePaneWidth

        Row(Modifier.fillMaxSize()) {
            if (showNavigationRail) {
                PrimaryNavigationRail(
                    navController = navController,
                    currentDestination = currentDestination,
                )
            }
            if (showReaderPane) {
                AdaptiveReaderPane(
                    navController = navController,
                    themeRepo = themeRepo,
                    prefsRepo = prefsRepo,
                    adaptiveWindowInfo = adaptiveWindowInfo,
                    modifier = Modifier.weight(leftPaneWeight),
                )
                if (adaptiveWindowInfo.hasVerticalSeparatingHinge) {
                    Spacer(Modifier.width(paneGap))
                } else {
                    androidx.compose.material3.VerticalDivider(
                        modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                    )
                }
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(if (showReaderPane) 1f - leftPaneWeight else 1f),
            ) {
                mainGraph(
                    navController = navController,
                    themeRepo = themeRepo,
                    prefsRepo = prefsRepo,
                    mainViewModel = mainViewModel,
                    adaptiveWindowInfo = adaptiveWindowInfo,
                    onExpand = onExpandCurrentScreen.takeIf { showReaderPane },
                )
                settingsGraph(
                    navController = navController,
                    themeRepo = themeRepo,
                    mainViewModel = mainViewModel,
                    adaptiveWindowInfo = adaptiveWindowInfo,
                )
                searchGraph(
                    navController = navController,
                    onExpand = onExpandCurrentScreen.takeIf { showReaderPane },
                )
                miscGraph(
                    navController = navController,
                    prefsRepo = prefsRepo
                )
            }
        }
    }
}
