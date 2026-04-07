package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific video player composable.
 * Android: ExoPlayer via AndroidView
 * iOS: AVPlayer via UIKitView
 */
@Composable
expect fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    onProgressChanged: ((Long) -> Unit)? = null,
)
