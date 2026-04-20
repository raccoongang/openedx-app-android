package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific WebView composable.
 * Android: Android WebView via AndroidView
 * iOS: WKWebView via UIKitView
 *
 * @param url The URL to load. Ignored when [htmlContent] is provided.
 * @param htmlContent Raw HTML to load via loadDataWithBaseURL / loadHTMLString.
 *                    When non-null, [url] is used only as the base URL for resolving relative links.
 * @param injectJSList JS snippets evaluated after [onPageFinished].
 * @param jsonProgress If non-empty, `markProblemCompleted('<json>')` is invoked after injection
 *                     to restore offline xBlock progress.
 * @param onPageStarted Fires when the WebView begins loading a page (or sets Loading state).
 * @param onPageError Fires when the main document load fails (connection / HTTP error).
 * @param onCompletionSet Fires when the xBlock JS calls `callback.completionSet()`.
 *                        Android: @JavascriptInterface; iOS: WKScriptMessageHandler "callback".
 * @param onPostMessage Fires when xBlock JS calls `AndroidBridge.postMessage(json)` — used to
 *                      persist offline xBlock progress. iOS: WKScriptMessageHandler "AndroidBridge".
 * @param isDarkMode When true, applies dark-mode CSS to rendered HTML content.
 */
@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageFinished: (() -> Unit)? = null,
    htmlContent: String? = null,
    injectJSList: List<String> = emptyList(),
    jsonProgress: String? = null,
    onPageStarted: (() -> Unit)? = null,
    onPageError: (() -> Unit)? = null,
    onCompletionSet: (() -> Unit)? = null,
    onPostMessage: ((String) -> Unit)? = null,
    isDarkMode: Boolean = false,
)
