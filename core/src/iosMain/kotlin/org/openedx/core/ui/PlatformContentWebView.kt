package org.openedx.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
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
}
