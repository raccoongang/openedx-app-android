package org.openedx.discovery.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openedx.core.system.AppCookieManager
import org.openedx.shared.ui.PlatformWebView

@Composable
actual fun CatalogPlatformWebView(
    url: String,
    uriScheme: String,
    userAgent: String,
    isAllLinksExternal: Boolean,
    modifier: Modifier,
    cookieManager: AppCookieManager?,
    onWebPageLoaded: () -> Unit,
    onWebPageUpdated: (String) -> Unit,
    onUriClick: (String, WebViewLink.Authority) -> Unit,
    onWebPageLoadError: () -> Unit,
) {
    PlatformWebView(
        url = url,
        modifier = modifier,
        onPageFinished = onWebPageLoaded,
    )
}
