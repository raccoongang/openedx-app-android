package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific YouTube video player.
 * Android: pierfrancescosoffritti/android-youtube-player library with DefaultPlayerUiController
 * iOS: WKWebView with YouTube IFrame Player API (native fullscreen via isHTMLElementFullscreenEnabled)
 */
@Composable
expect fun PlatformYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    startSeconds: Float = 0f,
    onReady: ((Any) -> Unit)? = null,
    onStateChange: ((Boolean) -> Unit)? = null,
    onCurrentSecond: ((Float) -> Unit)? = null,
    onVideoDuration: ((Float) -> Unit)? = null,
    onFullscreenClick: (() -> Unit)? = null,
)
