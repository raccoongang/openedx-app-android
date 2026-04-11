package org.openedx.foundation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberWindowSize(): WindowSize {
    val windowInfo = LocalWindowInfo.current
    val widthDp = windowInfo.containerSize.width
    val heightDp = windowInfo.containerSize.height
    return WindowSize(
        width = getScreenWidth(widthDp),
        height = getScreenHeight(heightDp),
        screenWidthDp = widthDp,
        screenHeightDp = heightDp,
    )
}
