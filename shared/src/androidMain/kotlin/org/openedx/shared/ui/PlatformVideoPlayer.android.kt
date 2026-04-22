package org.openedx.shared.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val PROGRESS_TICK_MS = 500L

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    startPositionMs: Long,
    maxVideoHeight: Int,
    onProgressChanged: ((Long) -> Unit)?,
    onEnded: (() -> Unit)?,
    onPlayPauseChanged: ((isPlaying: Boolean) -> Unit)?,
    onSpeedChanged: ((speed: Float) -> Unit)?,
    onVideoDuration: ((durationMs: Long) -> Unit)?,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentProgress by rememberUpdatedState(onProgressChanged)
    val currentEnded by rememberUpdatedState(onEnded)
    val currentPlayPause by rememberUpdatedState(onPlayPauseChanged)
    val currentSpeed by rememberUpdatedState(onSpeedChanged)
    val currentDuration by rememberUpdatedState(onVideoDuration)

    val exoPlayer = remember(url, maxVideoHeight) {
        val selector = DefaultTrackSelector(context).apply {
            if (maxVideoHeight > 0) {
                parameters = parameters.buildUpon()
                    .setMaxVideoSize(Int.MAX_VALUE, maxVideoHeight)
                    .setViewportSize(Int.MAX_VALUE, maxVideoHeight, false)
                    .build()
            }
        }
        ExoPlayer.Builder(context).setTrackSelector(selector).build().apply {
            val mediaMetadata = MediaMetadata.Builder()
                .setMediaType(MediaMetadata.MEDIA_TYPE_MOVIE)
                .build()
            val mediaItem = MediaItem.Builder()
                .setMediaMetadata(mediaMetadata)
                .setUri(Uri.parse(url))
                .build()

            if (url.contains(".m3u8")) {
                val factory = DefaultDataSource.Factory(context)
                val hlsSource = HlsMediaSource.Factory(factory)
                    .createMediaSource(mediaItem)
                setMediaSource(hlsSource, startPositionMs)
            } else {
                setMediaItem(mediaItem, startPositionMs)
            }
            prepare()
            playWhenReady = isPlaying
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0) currentDuration?.invoke(dur)
                }
                if (playbackState == Player.STATE_ENDED) {
                    currentEnded?.invoke()
                }
            }

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                currentPlayPause?.invoke(isPlayingNow)
            }

            override fun onPlaybackParametersChanged(params: PlaybackParameters) {
                currentSpeed?.invoke(params.speed)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (exoPlayer.isPlaying) {
                currentProgress?.invoke(exoPlayer.currentPosition)
            }
            delay(PROGRESS_TICK_MS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
        )
    }
}
