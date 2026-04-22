package org.openedx.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import org.openedx.core.presentation.global.InsetHolder

actual fun Modifier.statusBarsInset(): Modifier = composed {
    val topInset = (LocalContext.current as? InsetHolder)?.topInset ?: 0
    return@composed this
        .padding(top = with(LocalDensity.current) { topInset.toDp() })
}
