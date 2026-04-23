package org.openedx.foundation.presentation

enum class WindowType { Compact, Medium, Expanded }

data class WindowSize(
    val width: WindowType,
    val height: WindowType,
    val screenWidthDp: Int = 0,
    val screenHeightDp: Int = 0,
) {
    val isTablet: Boolean
        get() = height != WindowType.Compact && width != WindowType.Compact
    val isLandscape: Boolean
        get() = screenWidthDp > screenHeightDp
}

fun <T> WindowSize.windowSizeValue(expanded: T, compact: T): T {
    return if (height != WindowType.Compact && width != WindowType.Compact) {
        expanded
    } else {
        compact
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

private const val COMPACT_MAX_WIDTH = 600
private const val MEDIUM_MAX_WIDTH = 840
private const val COMPACT_MAX_HEIGHT = 480
private const val MEDIUM_MAX_HEIGHT = 900
