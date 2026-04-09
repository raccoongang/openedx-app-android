package org.openedx.foundation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = remember { mutableIntStateOf(configuration.screenWidthDp) }
    val screenHeight = remember { mutableIntStateOf(configuration.screenHeightDp) }
    screenWidth.intValue = configuration.screenWidthDp
    screenHeight.intValue = configuration.screenHeightDp
    return WindowSize(
        width = getScreenWidth(screenWidth.intValue),
        height = getScreenHeight(screenHeight.intValue),
    )
}
