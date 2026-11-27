package com.biblelib.core.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.biblelib.core.ui.utils.AdaptiveWidthClass
import com.biblelib.core.ui.utils.rememberAdaptiveWindowInfo

// Centers wide-screen content within a readable maximum width.
@Composable
fun CenteredContentFrame(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 840.dp,
    hasNavigationRail: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val windowInfo = rememberAdaptiveWindowInfo()
    BoxWithConstraints(modifier) {
        if (windowInfo.hasVerticalSeparatingHinge) {
            val railWidth = if (hasNavigationRail &&
                windowInfo.widthClass >= AdaptiveWidthClass.MEDIUM
            ) 80.dp else 0.dp
            val paneWidth = ((this.maxWidth - railWidth - windowInfo.verticalHingeGutter) / 2)
                .coerceAtLeast(0.dp)
            Box(
                modifier = Modifier.widthIn(max = minOf(maxWidth, paneWidth))
                    .fillMaxWidth().fillMaxHeight(),
                content = content,
            )
        } else {
            Box(
                modifier = Modifier.widthIn(max = maxWidth).fillMaxSize()
                    .align(Alignment.TopCenter),
                content = content,
            )
        }
    }
}
