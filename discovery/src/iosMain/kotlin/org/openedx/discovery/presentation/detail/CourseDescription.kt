package org.openedx.discovery.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CourseDescription(
    modifier: Modifier,
    apiHostUrl: String,
    body: String,
    onWebPageLoaded: () -> Unit,
) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView().apply {
                val request = NSURLRequest(uRL = NSURL(string = apiHostUrl))
                loadRequest(request)
            }
        },
    )
}
