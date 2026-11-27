package com.biblelib.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biblelib.core.common.utils.Routes
import com.biblelib.feature.scripture_opener.lists.view.screens.ScriptureListDetailScreen
import com.biblelib.feature.scripture_opener.lists.view.screens.ScriptureListScreen
import com.biblelib.feature.scripture_opener.lists.viewmodel.ScriptureListDetailViewModel
import com.biblelib.feature.scripture_opener.lists.viewmodel.ScriptureListsViewModel
import com.biblelib.feature.scripture_opener.opener.view.screens.ScriptureOpenerScreen
import com.biblelib.feature.scripture_opener.opener.viewmodel.ScriptureOpenerViewModel
import com.biblelib.feature.search.view.screen.SearchScreen
import com.biblelib.feature.search.viewmodel.SearchViewModel

// Registers Search and its related scripture destinations.
fun NavGraphBuilder.searchGraph(
    navController: NavHostController,
    onExpand: (() -> Unit)?,
    isAdaptiveLayout: Boolean,
    onOpenInReader: ((String, String, String, String, String) -> Unit)?,
) {
    composable(Routes.SEARCH) {
        val viewModel: SearchViewModel = hiltViewModel()
        SearchScreen(
            navController = navController,
            viewModel = viewModel,
            onExpand = onExpand,
            isAdaptiveLayout = isAdaptiveLayout,
            onOpenInReader = onOpenInReader,
        )
    }

    composable(
        route = Routes.SCRIPTURE_OPENER,
        arguments = listOf(
            navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
            navArgument("bibleName") { type = NavType.StringType; defaultValue = "" },
        ),
    ) { backStackEntry ->
        val bibleAbbr = backStackEntry.arguments?.getString("bibleAbbr") ?: ""
        val bibleName = Routes.decode(backStackEntry.arguments?.getString("bibleName") ?: "")
        val viewModel: ScriptureOpenerViewModel = hiltViewModel()
        ScriptureOpenerScreen(
            navController = navController,
            viewModel = viewModel,
            bibleAbbr = bibleAbbr,
            bibleName = bibleName,
            onExpand = onExpand,
        )
    }

    composable(Routes.SCRIPTURE_LISTS) {
        val viewModel: ScriptureListsViewModel = hiltViewModel()
        ScriptureListScreen(
            navController = navController,
            viewModel = viewModel,
            onExpand = onExpand,
        )
    }

    composable(
        route = Routes.SCRIPTURE_LIST_DETAIL,
        arguments = listOf(
            navArgument("listId") { type = NavType.LongType },
        ),
    ) { backStackEntry ->
        val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
        val viewModel: ScriptureListDetailViewModel = hiltViewModel()
        ScriptureListDetailScreen(
            navController = navController,
            viewModel = viewModel,
            listId = listId,
            onExpand = onExpand,
        )
    }
}
