package org.openedx.core.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS uses UINavigationController swipe-back; in-page WebView history traversal
    // is exposed through the screen-level back button instead.
}
