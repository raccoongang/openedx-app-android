package org.openedx.shared.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.openedx.core.config.Config
import org.openedx.core.extension.loadUrl
import org.openedx.core.system.AppCookieManager
import org.openedx.core.ui.theme.appColors
import org.openedx.foundation.extension.applyDarkModeIfEnabled

@Composable
@SuppressLint("SetJavaScriptEnabled")
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
    val context = LocalContext.current
    val cookieManager: AppCookieManager = koinInject()
    val config: Config = koinInject()
    val apiHostURL = config.getApiHostURL()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    val applyDark = isDarkMode || isSystemInDarkTheme()

    val pendingFileCallback = remember { arrayOfNulls<ValueCallback<Array<Uri>>>(1) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uris = WebChromeClient.FileChooserParams
            .parseResult(result.resultCode, result.data)
        pendingFileCallback[0]?.onReceiveValue(uris)
        pendingFileCallback[0] = null
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    if (onCompletionSet != null) {
                        addJavascriptInterface(
                            object {
                                @Suppress("unused")
                                @JavascriptInterface
                                fun completionSet() {
                                    onCompletionSet()
                                }
                            },
                            "callback",
                        )
                    }
                    if (onPostMessage != null) {
                        addJavascriptInterface(
                            object {
                                @Suppress("unused")
                                @JavascriptInterface
                                fun postMessage(str: String) {
                                    onPostMessage(str)
                                }
                            },
                            "AndroidBridge",
                        )
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?,
                        ): Boolean {
                            pendingFileCallback[0]?.onReceiveValue(null)
                            pendingFileCallback[0] = filePathCallback
                            val intent = try {
                                fileChooserParams?.createIntent()
                            } catch (_: Exception) {
                                null
                            } ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(
                                    Intent.EXTRA_ALLOW_MULTIPLE,
                                    fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE,
                                )
                            }
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            return try {
                                fileChooserLauncher.launch(intent)
                                true
                            } catch (_: Exception) {
                                pendingFileCallback[0] = null
                                false
                            }
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            onPageStarted?.invoke()
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                            isLoading = false
                            onPageFinished?.invoke()
                            if (injectJSList.isNotEmpty()) {
                                injectJSList.forEach { view?.evaluateJavascript(it, null) }
                                if (!jsonProgress.isNullOrEmpty()) {
                                    view?.loadUrl("javascript:markProblemCompleted('$jsonProgress');")
                                }
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val clickUrl = request?.url?.toString() ?: ""
                            if (clickUrl.startsWith(apiHostURL)) return false
                            if (clickUrl.contains("youtube.com") || clickUrl.contains("youtu.be") ||
                                clickUrl.contains("youtube-nocookie.com")
                            ) return false
                            if (clickUrl.startsWith("http") || clickUrl.startsWith("mailto:")) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl)))
                                }
                                return true
                            }
                            return false
                        }

                        override fun onReceivedHttpError(
                            view: WebView,
                            request: WebResourceRequest,
                            errorResponse: WebResourceResponse,
                        ) {
                            if (request.url.toString().startsWith(apiHostURL)) {
                                when (errorResponse.statusCode) {
                                    401, 403, 404 -> {
                                        coroutineScope.launch {
                                            cookieManager.tryToRefreshSessionCookie()
                                            loadUrl(url)
                                        }
                                    }
                                }
                            }
                            super.onReceivedHttpError(view, request, errorResponse)
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError,
                        ) {
                            if (request.url.toString() == view.url) {
                                isLoading = false
                                onPageError?.invoke()
                            }
                            super.onReceivedError(view, request, error)
                        }
                    }
                    with(settings) {
                        javaScriptEnabled = true
                        loadWithOverviewMode = true
                        builtInZoomControls = false
                        setSupportZoom(true)
                        loadsImagesAutomatically = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        mediaPlaybackRequiresUserGesture = false
                    }
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    if (htmlContent != null) {
                        loadDataWithBaseURL(
                            url.ifEmpty { null },
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null,
                        )
                    } else if (url.contains("youtube.com") || url.contains("youtu.be")) {
                        loadUrl(url)
                    } else {
                        loadUrl(url, coroutineScope, cookieManager)
                    }
                    applyDarkModeIfEnabled(applyDark)
                }
            },
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.appColors.primary,
                    strokeWidth = 4.dp,
                )
            }
        }
    }
}
