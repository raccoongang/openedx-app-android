package org.openedx.discovery.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openedx.core.system.AppCookieManager

@Composable
expect fun CatalogPlatformWebView(
    url: String,
    uriScheme: String,
    userAgent: String,
    isAllLinksExternal: Boolean = false,
    modifier: Modifier = Modifier,
    cookieManager: AppCookieManager? = null,
    onWebPageLoaded: () -> Unit = {},
    onWebPageUpdated: (String) -> Unit = {},
    onUriClick: (String, WebViewLink.Authority) -> Unit = { _, _ -> },
    onWebPageLoadError: () -> Unit = {},
)
