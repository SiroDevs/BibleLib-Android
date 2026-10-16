package com.biblelib.feature.settings.view.components

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biblelib.core.common.utils.CrossPromo
import com.biblelib.core.common.utils.openAppOrPlayStore
import com.biblelib.core.common.utils.openUrl
import com.biblelib.core.ui.R as UiR

private data class SiroApp(
    val name: String,
    @DrawableRes val icon: Int,
    val onOpen: (Context) -> Unit,
)

private val siroApps = listOf(
    SiroApp("SongLib", UiR.drawable.also_songlib) {
        openAppOrPlayStore(it, CrossPromo.SONGLIB_PACKAGE)
    },
    SiroApp("ChurchLib", UiR.drawable.also_churchlib) {
        openUrl(it, CrossPromo.CHURCHLIB_URL)
    },
    SiroApp("SwahiLib", UiR.drawable.also_swahilib) {
        openAppOrPlayStore(it, CrossPromo.SWAHILIB_PACKAGE)
    },
)

/** Horizontal row of the other Siro Devs apps, shown under an "Also by Siro Devs" label. */
@Composable
fun AlsoBySiroApps() {
    val context = LocalContext.current
    SettingsRowCard {
        siroApps.forEach { app ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { app.onOpen(context) }
                    .padding(vertical = 6.dp),
            ) {
                Image(
                    painter = painterResource(app.icon),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
