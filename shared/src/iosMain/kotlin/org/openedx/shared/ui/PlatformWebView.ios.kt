package org.openedx.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.AppCookieManager
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIApplication
import platform.UIKit.UIView
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationResponse
import platform.WebKit.WKNavigationResponsePolicy
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    onPageFinished: (() -> Unit)?,
    htmlContent: String?,
    injectJSList: List<String>,
    jsonProgress: String?,
    onPageStarted: (() -> Unit)?,
    onPageError: (() -> Unit)?,
    onCompletionSet: (() -> Unit)?,
    onPostMessage: ((String) -> Unit)?,
    isDarkMode: Boolean,
) {
    val needsCookies = htmlContent == null

    var cookieReady by remember { mutableStateOf(!needsCookies) }
    val webViewRef = remember { mutableStateOf<WKWebView?>(null) }
    val spinnerRef = remember { mutableStateOf<UIActivityIndicatorView?>(null) }

    if (needsCookies) {
        LaunchedEffect(Unit) {
            runCatching {
                val cookies = KoinPlatform.getKoin().get<AppCookieManager>()
                if (cookies.isSessionCookieMissingOrExpired()) {
                    cookies.tryToRefreshSessionCookie()
                }
            }
            cookieReady = true
        }
    }

    // Poll WKWebView.isLoading → hide/show native spinner + fire JS injection on load
    LaunchedEffect(webViewRef.value, cookieReady) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val spinner = spinnerRef.value ?: return@LaunchedEffect
        var hasInjected = false
        while (true) {
            delay(200)
            if (!wv.isLoading()) {
                if (!spinner.isHidden()) {
                    spinner.stopAnimating()
                    spinner.setHidden(true)
                    onPageFinished?.invoke()
                }
                if (!hasInjected && injectJSList.isNotEmpty()) {
                    hasInjected = true
                    injectJSList.forEach { script ->
                        wv.evaluateJavaScript(script) { _, _ -> }
                    }
                    if (!jsonProgress.isNullOrEmpty()) {
                        wv.evaluateJavaScript("markProblemCompleted('$jsonProgress');") { _, _ -> }
                    }
                }
            } else {
                if (spinner.isHidden()) {
                    spinner.setHidden(false)
                    spinner.startAnimating()
                    onPageStarted?.invoke()
                }
            }
        }
    }

    if (!cookieReady) {
        UIKitView(
            modifier = modifier,
            factory = {
                val container = UIView()
                val spinner = UIActivityIndicatorView(UIActivityIndicatorViewStyleLarge)
                spinner.setTranslatesAutoresizingMaskIntoConstraints(false)
                container.addSubview(spinner)
                spinner.centerXAnchor.constraintEqualToAnchor(container.centerXAnchor).setActive(true)
                spinner.centerYAnchor.constraintEqualToAnchor(container.centerYAnchor).setActive(true)
                spinner.startAnimating()
                container
            },
        )
        return
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val config = WKWebViewConfiguration()
            config.allowsInlineMediaPlayback = true
            config.mediaTypesRequiringUserActionForPlayback = 0u

            val userContent: WKUserContentController = config.userContentController
            val bridgeHandler = BridgeMessageHandler(
                onCompletionSet = onCompletionSet,
                onPostMessage = onPostMessage,
            )
            if (onCompletionSet != null) {
                userContent.addScriptMessageHandler(bridgeHandler, name = "callback")
                // Polyfill so xBlock JS that calls `callback.completionSet()` (Android bridge shape)
                // works on iOS via the WKScriptMessageHandler named "callback".
                userContent.addUserScript(
                    WKUserScript(
                        source = """
                            window.callback = window.callback || {};
                            window.callback.completionSet = function() {
                                window.webkit.messageHandlers.callback.postMessage("completionSet");
                            };
                        """.trimIndent(),
                        injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                        forMainFrameOnly = false,
                    )
                )
            }
            if (onPostMessage != null) {
                userContent.addScriptMessageHandler(bridgeHandler, name = "AndroidBridge")
                // Polyfill so xBlock JS that calls `AndroidBridge.postMessage(str)` works on iOS.
                userContent.addUserScript(
                    WKUserScript(
                        source = """
                            window.AndroidBridge = window.AndroidBridge || {};
                            window.AndroidBridge.postMessage = function(str) {
                                window.webkit.messageHandlers.AndroidBridge.postMessage(str);
                            };
                        """.trimIndent(),
                        injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
                        forMainFrameOnly = false,
                    )
                )
            }

            val wkWebView = WKWebView(frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config)

            val spinner = UIActivityIndicatorView(UIActivityIndicatorViewStyleLarge)
            spinner.setTranslatesAutoresizingMaskIntoConstraints(false)
            wkWebView.addSubview(spinner)
            spinner.centerXAnchor.constraintEqualToAnchor(wkWebView.centerXAnchor).setActive(true)
            spinner.centerYAnchor.constraintEqualToAnchor(wkWebView.centerYAnchor).setActive(true)
            spinner.startAnimating()

            webViewRef.value = wkWebView
            spinnerRef.value = spinner

            val appConfig = runCatching { KoinPlatform.getKoin().get<Config>() }.getOrNull()
            val apiHostURL = appConfig?.getApiHostURL() ?: ""

            val navDelegate = WebViewNavDelegate(
                originalUrl = url,
                apiHostURL = apiHostURL,
                webView = wkWebView,
                onPageError = onPageError,
            )
            wkWebView.navigationDelegate = navDelegate

            if (htmlContent != null) {
                wkWebView.loadHTMLString(htmlContent, baseURL = NSURL(string = url))
            } else {
                val nsUrl = NSURL(string = url) ?: return@UIKitView wkWebView
                val request = NSMutableURLRequest.requestWithURL(nsUrl)

                runCatching {
                    val prefs = KoinPlatform.getKoin().get<CorePreferences>()
                    val appCookies = KoinPlatform.getKoin().get<AppCookieManager>()
                    val token = prefs.accessToken
                    if (token.isNotEmpty() && appConfig != null) {
                        request.setValue(
                            "${appConfig.getAccessTokenType()} $token",
                            forHTTPHeaderField = "Authorization",
                        )
                    }
                    val cookieHeader =
                        (appCookies as? org.openedx.shared.stubs.IosAppCookieManager)
                            ?.sessionCookieHeader
                            .orEmpty()
                    if (cookieHeader.isNotEmpty()) {
                        request.setValue(cookieHeader, forHTTPHeaderField = "Cookie")
                    }
                }

                wkWebView.loadRequest(request)
            }

            wkWebView
        },
    )
}

private class BridgeMessageHandler(
    private val onCompletionSet: (() -> Unit)?,
    private val onPostMessage: ((String) -> Unit)?,
) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        when (didReceiveScriptMessage.name) {
            "callback" -> onCompletionSet?.invoke()
            "AndroidBridge" -> {
                val body = didReceiveScriptMessage.body
                val str = (body as? String) ?: body?.toString().orEmpty()
                onPostMessage?.invoke(str)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class WebViewNavDelegate(
    private val originalUrl: String,
    private val apiHostURL: String,
    private val webView: WKWebView,
    private val onPageError: (() -> Unit)?,
) : NSObject(), WKNavigationDelegateProtocol {

    private var hasRetried = false

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationResponse: WKNavigationResponse,
        decisionHandler: (WKNavigationResponsePolicy) -> Unit,
    ) {
        val response = decidePolicyForNavigationResponse.response as? platform.Foundation.NSHTTPURLResponse
        val statusCode = response?.statusCode?.toInt() ?: 200
        val responseUrl = response?.URL?.absoluteString ?: ""

        if ((statusCode in 401..404 || responseUrl.contains("/login")) &&
            responseUrl.startsWith(apiHostURL) && !hasRetried
        ) {
            hasRetried = true
            decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyCancel)
            kotlinx.coroutines.MainScope().launch {
                runCatching {
                    val cookies = KoinPlatform.getKoin().get<AppCookieManager>()
                    cookies.tryToRefreshSessionCookie()
                }
                val nsUrl = NSURL(string = originalUrl) ?: return@launch
                webView.loadRequest(NSMutableURLRequest.requestWithURL(nsUrl))
            }
            return
        }
        decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyAllow)
    }

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val navUrl = decidePolicyForNavigationAction.request.URL?.absoluteString ?: ""

        if (apiHostURL.isNotEmpty() && navUrl.startsWith(apiHostURL)) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }
        if (navUrl.contains("youtube.com") || navUrl.contains("youtu.be") ||
            navUrl.contains("youtube-nocookie.com") || navUrl.contains("googlevideo.com")
        ) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }
        if (navUrl.startsWith("about:") || navUrl.startsWith("data:")) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }

        val isExternalLink = navUrl.startsWith("http") &&
            decidePolicyForNavigationAction.navigationType.toInt() == 0

        if (isExternalLink || navUrl.startsWith("mailto:")) {
            val nsUrl = NSURL(string = navUrl)
            if (nsUrl != null) {
                UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>()) { _ -> }
            }
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            return
        }
        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }

    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: platform.WebKit.WKNavigation?,
        withError: platform.Foundation.NSError,
    ) {
        onPageError?.invoke()
    }
}
