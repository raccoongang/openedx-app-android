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
    startPositionMs: Long = 0L,
    maxVideoHeight: Int = 0,
    onProgressChanged: ((Long) -> Unit)? = null,
    onEnded: (() -> Unit)? = null,
    onPlayPauseChanged: ((isPlaying: Boolean) -> Unit)? = null,
    onSpeedChanged: ((speed: Float) -> Unit)? = null,
    onVideoDuration: ((durationMs: Long) -> Unit)? = null,
)
