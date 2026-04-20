package org.openedx.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.preferredMaximumResolution
import platform.AVFoundation.rate
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGSizeMake
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
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
) {
    val player = remember(url, maxVideoHeight) {
        val item = AVPlayerItem(uRL = NSURL(string = url))
        if (maxVideoHeight > 0) {
            item.preferredMaximumResolution = CGSizeMake(Double.MAX_VALUE, maxVideoHeight.toDouble())
        }
        AVPlayer(playerItem = item)
    }
    val currentProgress by rememberUpdatedState(onProgressChanged)
    val currentEnded by rememberUpdatedState(onEnded)
    val currentPlayPause by rememberUpdatedState(onPlayPauseChanged)
    val currentSpeed by rememberUpdatedState(onSpeedChanged)

    DisposableEffect(url, startPositionMs) {
        if (startPositionMs > 0) {
            player.seekToTime(CMTimeMakeWithSeconds(startPositionMs / 1000.0, 600))
        }
        onDispose {}
    }

    DisposableEffect(isPlaying) {
        if (isPlaying) player.play()
        onDispose {}
    }

    DisposableEffect(player) {
        val interval = CMTimeMakeWithSeconds(0.5, 600)
        var lastIsPlaying: Boolean? = null
        var lastNonZeroRate: Float = 1f
        val token = player.addPeriodicTimeObserverForInterval(
            interval = interval,
            queue = null,
            usingBlock = { time ->
                val seconds = CMTimeGetSeconds(time)
                if (!seconds.isNaN() && seconds >= 0) {
                    currentProgress?.invoke((seconds * 1000).toLong())
                }
                val rate = player.rate
                val playingNow = rate > 0f
                if (lastIsPlaying != playingNow) {
                    lastIsPlaying = playingNow
                    currentPlayPause?.invoke(playingNow)
                }
                if (rate > 0f && rate != lastNonZeroRate) {
                    lastNonZeroRate = rate
                    currentSpeed?.invoke(rate)
                }
            },
        )
        val endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _ -> currentEnded?.invoke() },
        )
        onDispose {
            player.removeTimeObserver(token)
            NSNotificationCenter.defaultCenter.removeObserver(endObserver)
        }
    }

    DisposableEffect(url) {
        onDispose { player.pause() }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val controller = AVPlayerViewController()
                controller.player = player
                controller.showsPlaybackControls = true
                controller.view.backgroundColor = UIColor.blackColor
                controller.view
            },
            update = {},
        )
    }
}

