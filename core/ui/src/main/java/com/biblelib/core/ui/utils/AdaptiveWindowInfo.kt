package com.biblelib.core.ui.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

// Names the supported width ranges used by responsive layouts.
enum class AdaptiveWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    LARGE,
    EXTRA_LARGE,
}

// Collects the current window size and fold posture for UI decisions.
data class AdaptiveWindowInfo(
    val widthClass: AdaptiveWidthClass,
    val isTabletop: Boolean,
    val hasVerticalSeparatingHinge: Boolean,
    val hasHorizontalSeparatingHinge: Boolean,
    val verticalHingeGutter: Dp,
    val horizontalHingeClearance: Dp,
)

@Composable
// Reads current window size and separating hinge information.
fun rememberAdaptiveWindowInfo(): AdaptiveWindowInfo {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val density = LocalDensity.current
    val windowSizeClass = adaptiveInfo.windowSizeClass
    val posture = adaptiveInfo.windowPosture
    val widthClass = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) ->
            AdaptiveWidthClass.EXTRA_LARGE

        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND) ->
            AdaptiveWidthClass.LARGE

        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            AdaptiveWidthClass.EXPANDED

        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            AdaptiveWidthClass.MEDIUM

        else -> AdaptiveWidthClass.COMPACT
    }
    val verticalHingeBounds = posture.separatingVerticalHingeBounds
    val horizontalHingeBounds = posture.separatingHorizontalHingeBounds
    val verticalHingeGutter = verticalHingeBounds.maxOfOrNull { hinge ->
        maxOf(80.dp, with(density) { hinge.width.toDp() + 32.dp })
    } ?: 24.dp
    val horizontalHingeClearance = horizontalHingeBounds.maxOfOrNull { hinge ->
        with(density) { hinge.height.toDp() }
    } ?: 0.dp

    return AdaptiveWindowInfo(
        widthClass = widthClass,
        isTabletop = posture.isTabletop,
        hasVerticalSeparatingHinge = verticalHingeBounds.isNotEmpty(),
        hasHorizontalSeparatingHinge = horizontalHingeBounds.isNotEmpty(),
        verticalHingeGutter = verticalHingeGutter,
        horizontalHingeClearance = horizontalHingeClearance,
    )
}
