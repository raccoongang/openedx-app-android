package org.openedx.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openedx.shared.ui.PlatformWebView

@Composable
actual fun PlatformContentWebView(
    modifier: Modifier,
    apiHostUrl: String?,
    body: String?,
    contentUrl: String?,
    onWebPageLoaded: () -> Unit,
) {
    val url = contentUrl ?: apiHostUrl ?: ""
    if (url.isNotEmpty()) {
        PlatformWebView(
            url = url,
            modifier = modifier,
            onPageFinished = onWebPageLoaded,
        )
    }
}
