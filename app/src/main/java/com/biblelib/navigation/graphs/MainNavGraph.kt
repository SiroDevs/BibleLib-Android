package com.biblelib.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.viewmodel.MainViewModel
import com.biblelib.feature.bibles.view.screens.BiblesScreen
import com.biblelib.feature.bibles.viewmodel.BiblesViewModel
import com.biblelib.feature.bookmark_notes.view.BookmarkNotesScreen
import com.biblelib.feature.bookmark_notes.viewmodel.BookmarkNotesViewModel
import com.biblelib.feature.casting.view.screen.CastingScreen
import com.biblelib.feature.casting.viewmodel.CastingViewModel
import com.biblelib.feature.history.view.HistoryScreen
import com.biblelib.feature.history.viewmodel.HistoryViewModel
import com.biblelib.feature.reader.home.view.screens.ReaderScreen
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel
import com.biblelib.feature.reader.notes.view.NotesScreen
import com.biblelib.feature.reader.notes.viewmodel.NotesViewModel
import com.biblelib.feature.selection.view.screen.SelectionScreen
import com.biblelib.feature.selection.viewmodel.SelectionViewModel

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    themeRepo: ThemeRepo,
    mainViewModel: MainViewModel,
) {
    composable(Routes.SELECTION) {
        val viewModel: SelectionViewModel = hiltViewModel()
        SelectionScreen(
            navController = navController,
            viewModel = viewModel,
            themeRepo = themeRepo,
        )
    }

    composable(
        route = Routes.READER,
        arguments = listOf(
            navArgument("bibleName") { type = NavType.StringType; defaultValue = "" },
            navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
            navArgument("bookId") { type = NavType.StringType; defaultValue = "" },
            navArgument("chapterId") { type = NavType.StringType; defaultValue = "" },
            navArgument("verseId") { type = NavType.StringType; defaultValue = "" },
            navArgument("searchQuery") { type = NavType.StringType; defaultValue = "" },
        )
    ) { backStackEntry ->
        val bibleName = backStackEntry.arguments?.getString("bibleName") ?: ""
        val bibleAbbr = backStackEntry.arguments?.getString("bibleAbbr") ?: ""
        val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
        val verseId = Routes.decode(backStackEntry.arguments?.getString("verseId") ?: "")
        val searchQuery = Routes.decode(backStackEntry.arguments?.getString("searchQuery") ?: "")
        val viewModel: ReaderViewModel = hiltViewModel()
        ReaderScreen(
            navController = navController,
            viewModel = viewModel,
            initialBible = bibleName,
            initialBibleAbbr = bibleAbbr,
            initialBookId = bookId,
            initialChapterId = chapterId,
            initialVerseId = verseId,
            initialSearchQry = searchQuery,
            themeRepo = themeRepo,
        )
    }

    composable(
        route = Routes.NOTES,
        arguments = listOf(
            navArgument("bibleAbbr") { type = NavType.StringType; defaultValue = "" },
            navArgument("verseId") { type = NavType.StringType; defaultValue = "" },
            navArgument("bookId") { type = NavType.StringType; defaultValue = "" },
            navArgument("chapterId") { type = NavType.StringType; defaultValue = "" },
            navArgument("title") { type = NavType.StringType; defaultValue = "" },
            navArgument("verseText") { type = NavType.StringType; defaultValue = "" },
        )
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val bibleAbbr = args?.getString("bibleAbbr") ?: ""
        val verseId = Routes.decode(args?.getString("verseId") ?: "")
        val bookId = Routes.decode(args?.getString("bookId") ?: "")
        val chapterId = Routes.decode(args?.getString("chapterId") ?: "")
        val title = Routes.decode(args?.getString("title") ?: "")
        val verseText = Routes.decode(args?.getString("verseText") ?: "")
        val viewModel: NotesViewModel = hiltViewModel()
        NotesScreen(
            navController = navController,
            viewModel = viewModel,
            bibleAbbr = bibleAbbr,
            verseId = verseId,
            bookId = bookId,
            chapterId = chapterId,
            title = title,
            verseText = verseText,
        )
    }

    composable(Routes.HISTORY) {
        val viewModel: HistoryViewModel = hiltViewModel()
        HistoryScreen(
            navController = navController,
            viewModel = viewModel,
        )
    }

    composable(Routes.BOOKMARKS_NOTES) {
        val viewModel: BookmarkNotesViewModel = hiltViewModel()
        BookmarkNotesScreen(
            navController = navController,
            viewModel = viewModel,
        )
    }

    composable(Routes.BIBLES) {
        val biblesVm: BiblesViewModel = hiltViewModel()
        BiblesScreen(
            navController = navController,
            mainViewModel = mainViewModel,
            viewModel = biblesVm,
        )
    }

    composable(Routes.CASTING) {
        val castingVm: CastingViewModel = hiltViewModel()
        CastingScreen(
            navController = navController,
            viewModel = castingVm,
        )
    }
}
