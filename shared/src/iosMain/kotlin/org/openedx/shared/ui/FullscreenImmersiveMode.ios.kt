package org.openedx.shared.ui

import androidx.compose.runtime.Composable

@Composable
actual fun EnterFullscreenImmersiveMode() {
    // iOS has no navigation bar overlap; the native fullscreen behaviour is
    // driven by AVPlayerViewController / the YouTube iframe, so nothing to do.
}
