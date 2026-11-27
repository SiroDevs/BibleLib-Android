package com.biblelib.core.ui.components.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.util.Locale.getDefault
import androidx.compose.ui.platform.LocalLocale
import com.biblelib.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
// Displays a shared app bar with optional back or close navigation.
@Composable
fun AppTopBar(
    title: String,
    tagline: String? = null,
    showGoBack: Boolean = false,
    closeOnBack: Boolean = false,
    titleMaxLines: Int = 1,
    onNavIconClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
                if (!tagline.isNullOrBlank()) {
                    Text(
                        text = tagline.uppercase(LocalLocale.current.platformLocale),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        navigationIcon = {
            if (showGoBack) {
                IconButton(onClick = { onNavIconClick?.invoke() }) {
                    Icon(
                        imageVector = if (closeOnBack) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            if (closeOnBack) R.string.adaptive_close_pane else R.string.adaptive_back,
                        ),
                    )
                }
            }
        }
    )
}
