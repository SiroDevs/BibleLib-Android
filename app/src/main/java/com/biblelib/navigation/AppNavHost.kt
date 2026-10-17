package com.biblelib.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.navigation.graphs.mainGraph
import com.biblelib.navigation.graphs.miscGraph
import com.biblelib.navigation.graphs.searchGraph
import com.biblelib.navigation.graphs.settingsGraph

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    themeRepo: ThemeRepo,
    prefsRepo: PrefsRepo,
    mainViewModel: MainViewModel,
) {
    val isReady by mainViewModel.isReady.collectAsState()
    val destination by mainViewModel.destination.collectAsState()

    if (!isReady) return

    val startDestination = when (destination) {
        is MainViewModel.Destination.Selection -> Routes.SELECTION
        is MainViewModel.Destination.Reader -> Routes.READER
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        mainGraph(
            navController = navController,
            themeRepo = themeRepo,
            prefsRepo = prefsRepo,
            mainViewModel = mainViewModel,
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
