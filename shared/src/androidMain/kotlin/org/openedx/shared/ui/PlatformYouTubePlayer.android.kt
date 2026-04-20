package org.openedx.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
actual fun PlatformYouTubePlayer(
    videoId: String,
    modifier: Modifier,
    startSeconds: Float,
    onReady: ((Any) -> Unit)?,
    onStateChange: ((Boolean) -> Unit)?,
    onCurrentSecond: ((Float) -> Unit)?,
    onVideoDuration: ((Float) -> Unit)?,
    onFullscreenClick: (() -> Unit)?,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val youtubePlayerView = remember {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(youtubePlayerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youtubePlayerView)
        }
    }

    DisposableEffect(videoId) {
        // controls(0) — hide YouTube's built-in controls, use DefaultPlayerUiController instead
        // (matches original YoutubeVideoUnitFragment)
        val options = IFramePlayerOptions.Builder(context)
            .controls(0)
            .rel(0)
            .build()

        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                // Set up custom UI controller with fullscreen button (like original)
                val uiController = DefaultPlayerUiController(
                    youtubePlayerView,
                    youTubePlayer,
                )
                uiController.setFullscreenButtonClickListener {
                    onFullscreenClick?.invoke()
                }
                youtubePlayerView.setCustomPlayerUi(uiController.rootView)

                onReady?.invoke(youTubePlayer)
                youTubePlayer.loadVideo(videoId, startSeconds)
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState,
            ) {
                when (state) {
                    PlayerConstants.PlayerState.PLAYING -> onStateChange?.invoke(true)
                    PlayerConstants.PlayerState.PAUSED -> onStateChange?.invoke(false)
                    else -> {}
                }
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onCurrentSecond?.invoke(second)
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                onVideoDuration?.invoke(duration)
            }
        }

        youtubePlayerView.initialize(listener, options)

        onDispose {}
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { youtubePlayerView },
        )
    }
}
