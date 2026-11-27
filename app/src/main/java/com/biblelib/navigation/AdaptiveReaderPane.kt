package com.biblelib.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.utils.AdaptiveWindowInfo
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.feature.reader.home.view.screens.ReaderScreen
import com.biblelib.feature.reader.home.viewmodel.ReaderViewModel

// Reuses the active reader destination inside an adaptive workspace pane.
@Composable
fun AdaptiveReaderPane(
    navController: NavHostController,
    themeRepo: ThemeRepo,
    prefsRepo: PrefsRepo,
    adaptiveWindowInfo: AdaptiveWindowInfo,
    modifier: Modifier = Modifier,
) {
    val readerEntry = navController.getBackStackEntry(Routes.READER)
    val arguments = readerEntry.arguments
    val viewModel: ReaderViewModel = hiltViewModel(readerEntry)
    val paneWindowInfo = adaptiveWindowInfo.copy(
        widthClass = AdaptiveWidthClass.MEDIUM,
        hasVerticalSeparatingHinge = false,
        hasHorizontalSeparatingHinge = false,
        verticalHingeGutter = 24.dp,
        horizontalHingeClearance = 0.dp,
    )

    Box(modifier) {
        ReaderScreen(
            navController = navController,
            viewModel = viewModel,
            initialBible = "",
            initialBibleAbbr = arguments?.getString("bibleAbbr").orEmpty(),
            initialBookId = arguments?.getString("bookId").orEmpty(),
            initialChapterId = arguments?.getString("chapterId").orEmpty(),
            initialVerseId = Routes.decode(arguments?.getString("verseId").orEmpty()),
            initialSearchQry = Routes.decode(arguments?.getString("searchQuery").orEmpty()),
            themeRepo = themeRepo,
            prefsRepo = prefsRepo,
            adaptiveWindowInfo = paneWindowInfo,
            initializeOnEntry = false,
        )
    }
}
