package org.openedx.foundation.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

enum class WindowType { Compact, Medium, Expanded }

data class WindowSize(
    val width: WindowType,
    val height: WindowType,
) {
    val isTablet: Boolean
        get() = width != WindowType.Compact
}

private const val COMPACT_MAX_WIDTH = 600
private const val MEDIUM_MAX_WIDTH = 840
private const val COMPACT_MAX_HEIGHT = 480
private const val MEDIUM_MAX_HEIGHT = 900

fun <T> WindowSize.windowSizeValue(expanded: T, compact: T): T {
    return when (width) {
        WindowType.Compact -> compact
        else -> expanded
    }
}

fun getScreenWidth(width: Int): WindowType = when {
    width < COMPACT_MAX_WIDTH -> WindowType.Compact
    width < MEDIUM_MAX_WIDTH -> WindowType.Medium
    else -> WindowType.Expanded
}

fun getScreenHeight(height: Int): WindowType = when {
    height < COMPACT_MAX_HEIGHT -> WindowType.Compact
    height < MEDIUM_MAX_HEIGHT -> WindowType.Medium
    else -> WindowType.Expanded
}

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
