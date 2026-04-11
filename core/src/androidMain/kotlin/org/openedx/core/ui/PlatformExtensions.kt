package org.openedx.core.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import org.openedx.core.presentation.global.InsetHolder

actual fun Modifier.statusBarsInset(): Modifier = composed {
    val topInset = (LocalContext.current as? InsetHolder)?.topInset ?: 0
    return@composed this
        .padding(top = with(LocalDensity.current) { topInset.toDp() })
}

actual fun Modifier.displayCutoutForLandscape(): Modifier = composed {
    val cutoutInset = (LocalContext.current as? InsetHolder)?.cutoutInset ?: 0
    val cutoutInsetDp = with(LocalDensity.current) { cutoutInset.toDp() }
    return@composed if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this.padding(horizontal = cutoutInsetDp)
    } else {
        this
    }
}
