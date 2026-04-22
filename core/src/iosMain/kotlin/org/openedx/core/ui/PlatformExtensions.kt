package org.openedx.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

actual fun Modifier.statusBarsInset(): Modifier = composed {
    this.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
}
