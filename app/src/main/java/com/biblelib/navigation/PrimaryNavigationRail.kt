package com.biblelib.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.biblelib.R
import com.biblelib.core.common.utils.Routes

// Centers top-level destinations in the adaptive navigation rail.
@Composable
fun PrimaryNavigationRail(
    navController: NavController,
    currentDestination: NavDestination?,
) {
    val destinations = listOf(
        PrimaryDestination(Routes.reader(), Routes.READER, Icons.AutoMirrored.Filled.MenuBook, R.string.navigation_read),
        PrimaryDestination(Routes.SEARCH, Routes.SEARCH, Icons.Default.Search, R.string.navigation_search),
        PrimaryDestination(Routes.HISTORY, Routes.HISTORY, Icons.Default.History, R.string.navigation_history),
        PrimaryDestination(
            Routes.BOOKMARKS_NOTES,
            Routes.BOOKMARKS_NOTES,
            Icons.Default.Bookmarks,
            R.string.navigation_bookmarks,
        ),
        PrimaryDestination(Routes.SETTINGS, Routes.SETTINGS, Icons.Default.Settings, R.string.navigation_settings),
    )
    val activeRoute = when (currentDestination?.route) {
        Routes.SEARCH, Routes.SCRIPTURE_OPENER, Routes.SCRIPTURE_LISTS,
        Routes.SCRIPTURE_LIST_DETAIL -> Routes.SEARCH
        Routes.HISTORY -> Routes.HISTORY
        Routes.BOOKMARKS_NOTES -> Routes.BOOKMARKS_NOTES
        Routes.SETTINGS, Routes.APPEARANCE_SETTINGS, Routes.READING_SETTINGS,
        Routes.DATA_SETTINGS -> Routes.SETTINGS
        else -> Routes.READER
    }

    NavigationRail {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
        ) {
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
                    label = { Text(stringResource(destination.label)) },
                    alwaysShowLabel = true,
                )
            }
        }
    }
}

// Describes one top-level navigation rail destination.
private data class PrimaryDestination(
    val route: String,
    val selectedRoute: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: Int,
)
