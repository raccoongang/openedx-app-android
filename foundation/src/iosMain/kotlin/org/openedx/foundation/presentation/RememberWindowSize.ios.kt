package org.openedx.foundation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberWindowSize(): WindowSize {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    // LocalWindowInfo.containerSize on iOS reports pixels — convert to dp so the same
    // COMPACT/MEDIUM/EXPANDED thresholds (used Android-side via configuration.screenWidthDp)
    // classify the device correctly. Without this, an iPhone (e.g. 1206×2622 px @3x)
    // would always be misclassified as "tablet".
    val widthDp = (windowInfo.containerSize.width / density.density).toInt()
    val heightDp = (windowInfo.containerSize.height / density.density).toInt()
    return WindowSize(
        width = getScreenWidth(widthDp),
        height = getScreenHeight(heightDp),
        screenWidthDp = widthDp,
        screenHeightDp = heightDp,
    )
}
