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
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
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
    val messageHandler = remember {
        YouTubeMessageHandler(
            onReady = onReady,
            onStateChange = onStateChange,
            onCurrentSecond = onCurrentSecond,
            onVideoDuration = onVideoDuration,
        )
    }

    val bundleId = remember {
        NSBundle.mainBundle.bundleIdentifier?.lowercase() ?: "org.openedx.app.ios"
    }
    val originUrl = remember(bundleId) { "https://$bundleId" }

    val htmlContent = remember(videoId, startSeconds, originUrl) {
        buildYouTubeIFrameApiHtml(videoId, startSeconds, originUrl)
    }

    val webView = remember(videoId) {
        val contentController = WKUserContentController()
        contentController.addScriptMessageHandler(messageHandler, "ytPlayer")

        val config = WKWebViewConfiguration()
        config.userContentController = contentController
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = 0u
        config.preferences.javaScriptCanOpenWindowsAutomatically = true

        WKWebView(
            frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
            configuration = config,
        ).apply {
            setOpaque(false)
            scrollView.scrollEnabled = false
            scrollView.bounces = false
            loadHTMLString(htmlContent, baseURL = NSURL(string = originUrl))
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.evaluateJavaScript(
                "if (typeof player !== 'undefined' && player && player.pauseVideo) { player.pauseVideo(); }",
                completionHandler = null,
            )
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView },
        )
    }
}

private class YouTubeMessageHandler(
    private val onReady: ((Any) -> Unit)?,
    private val onStateChange: ((Boolean) -> Unit)?,
    private val onCurrentSecond: ((Float) -> Unit)?,
    private val onVideoDuration: ((Float) -> Unit)?,
) : NSObject(), WKScriptMessageHandlerProtocol {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val body = didReceiveScriptMessage.body
        if (body is Map<*, *>) {
            val event = body["event"] as? String ?: return
            when (event) {
                "ready" -> onReady?.invoke(Unit)
                "stateChange" -> {
                    val state = (body["state"] as? Number)?.toInt() ?: return
                    // YouTube IFrame API states: 1=playing, 2=paused
                    when (state) {
                        1 -> onStateChange?.invoke(true)
                        2 -> onStateChange?.invoke(false)
                    }
                }
                "currentTime" -> {
                    val time = (body["time"] as? Number)?.toFloat() ?: return
                    onCurrentSecond?.invoke(time)
                }
                "duration" -> {
                    val dur = (body["duration"] as? Number)?.toFloat() ?: return
                    onVideoDuration?.invoke(dur)
                }
            }
        }
    }
}

private fun buildYouTubeIFrameApiHtml(videoId: String, startSeconds: Float, origin: String): String {
    val startInt = startSeconds.toInt()
    return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
* { margin: 0; padding: 0; }
html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
#player { width: 100%; height: 100%; }
</style>
</head>
<body>
<div id="player"></div>
<script>
var tag = document.createElement('script');
tag.src = "https://www.youtube.com/iframe_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

var player;
function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        videoId: '$videoId',
        playerVars: {
            'playsinline': 1,
            'rel': 0,
            'modestbranding': 1,
            'start': $startInt,
            'controls': 1,
            'fs': 1,
            'origin': '$origin'
        },
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}
function onPlayerReady(event) {
    window.webkit.messageHandlers.ytPlayer.postMessage({event: 'ready'});
    var dur = player.getDuration();
    if (dur > 0) {
        window.webkit.messageHandlers.ytPlayer.postMessage({event: 'duration', duration: dur});
    }
    setInterval(function() {
        if (player && player.getCurrentTime) {
            var t = player.getCurrentTime();
            window.webkit.messageHandlers.ytPlayer.postMessage({event: 'currentTime', time: t});
            var d = player.getDuration();
            if (d > 0) {
                window.webkit.messageHandlers.ytPlayer.postMessage({event: 'duration', duration: d});
            }
        }
    }, 500);
}
function onPlayerStateChange(event) {
    window.webkit.messageHandlers.ytPlayer.postMessage({event: 'stateChange', state: event.data});
}
</script>
</body>
</html>
""".trimIndent()
}
