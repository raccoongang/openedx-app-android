@file:JvmName("CommonPlatformExtensions")

package org.openedx.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlin.jvm.JvmName

expect fun Modifier.statusBarsInset(): Modifier

fun Modifier.displayCutoutForLandscape(): Modifier = composed {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insets = WindowInsets.safeDrawing
    val left = insets.getLeft(density, layoutDirection)
    val right = insets.getRight(density, layoutDirection)
    val cutoutDp = with(density) { maxOf(left, right).toDp() }
    padding(horizontal = cutoutDp)
}
