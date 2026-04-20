package org.openedx.discovery.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    backControl: WebViewBackControl?,
) {
    val webView = remember(url) {
        WKWebView().apply {
            val request = NSURLRequest(uRL = NSURL(string = url))
            loadRequest(request)
        }
    }
    DisposableEffect(webView, backControl) {
        if (backControl != null) {
            backControl.goBackImpl = { if (webView.canGoBack) webView.goBack() }
        }
        val scope: CoroutineScope = MainScope()
        scope.launch(Dispatchers.Main) {
            while (isActive) {
                backControl?.canGoBackState = webView.canGoBack
                delay(500)
            }
        }
        onDispose { scope.cancel() }
    }
    UIKitView(
        modifier = modifier,
        factory = { webView },
    )
}
