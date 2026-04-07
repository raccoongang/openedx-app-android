package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific WebView composable.
 * Android: Android WebView via AndroidView
 * iOS: WKWebView via UIKitView
 */
@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageFinished: (() -> Unit)? = null,
)
