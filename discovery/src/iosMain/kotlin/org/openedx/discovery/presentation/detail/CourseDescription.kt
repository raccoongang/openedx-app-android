package org.openedx.discovery.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openedx.shared.ui.PlatformWebView

@Composable
actual fun CourseDescription(
    modifier: Modifier,
    apiHostUrl: String,
    body: String,
    onWebPageLoaded: () -> Unit,
) {
    PlatformWebView(
        url = apiHostUrl,
        modifier = modifier,
        onPageFinished = onWebPageLoaded,
    )
}
