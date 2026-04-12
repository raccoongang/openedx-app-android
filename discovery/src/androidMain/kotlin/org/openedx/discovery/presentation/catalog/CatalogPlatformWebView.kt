package org.openedx.discovery.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.openedx.core.extension.loadUrl
import org.openedx.core.system.AppCookieManager
import org.openedx.core.ui.theme.appColors

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
    val coroutineScope = rememberCoroutineScope()
    val webView = CatalogWebViewScreen(
        url = url,
        uriScheme = uriScheme,
        userAgent = userAgent,
        isAllLinksExternal = isAllLinksExternal,
        onWebPageLoaded = onWebPageLoaded,
        refreshSessionCookie = {
            if (cookieManager != null) {
                coroutineScope.launch {
                    cookieManager.tryToRefreshSessionCookie()
                }
            }
        },
        onWebPageUpdated = onWebPageUpdated,
        onUriClick = onUriClick,
        onWebPageLoadError = onWebPageLoadError,
    )

    AndroidView(
        modifier = modifier.background(MaterialTheme.appColors.background),
        factory = { webView },
        update = {
            if (cookieManager != null) {
                webView.loadUrl(url, coroutineScope, cookieManager)
            }
        }
    )
}
