package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier,
    isPlaying: Boolean,
    onProgressChanged: ((Long) -> Unit)?,
) {
    val player = remember {
        AVPlayer(uRL = NSURL(string = url))
    }

    DisposableEffect(Unit) {
        if (isPlaying) player.play()
        onDispose {
            player.pause()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val controller = AVPlayerViewController()
            controller.player = player
            controller.view
        },
    )
}
