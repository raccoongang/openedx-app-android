package org.openedx.discovery.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import org.openedx.core.system.AppCookieManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
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
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView().apply {
                val request = NSURLRequest(uRL = NSURL(string = url))
                loadRequest(request)
            }
        },
    )
}
