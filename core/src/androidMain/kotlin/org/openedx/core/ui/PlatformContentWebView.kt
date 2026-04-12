package org.openedx.core.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.openedx.core.utils.EmailUtil
import org.openedx.foundation.extension.applyDarkModeIfEnabled
import org.openedx.foundation.extension.isEmailValid
import org.openedx.foundation.extension.replaceLinkTags
import java.nio.charset.StandardCharsets

@Composable
@SuppressLint("SetJavaScriptEnabled")
actual fun PlatformContentWebView(
    modifier: Modifier,
    apiHostUrl: String?,
    body: String?,
    contentUrl: String?,
    onWebPageLoaded: () -> Unit,
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        onWebPageLoaded()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val clickUrl = request?.url?.toString() ?: ""
                        return if (clickUrl.isNotEmpty() && clickUrl.startsWith("http")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(clickUrl)))
                            true
                        } else if (clickUrl.startsWith("mailto:")) {
                            val email = clickUrl.replace("mailto:", "")
                            if (email.isEmailValid()) {
                                EmailUtil.sendEmailIntent(context, email, "", "")
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    }
                }
                with(settings) {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    builtInZoomControls = false
                    setSupportZoom(true)
                    loadsImagesAutomatically = true
                    domStorageEnabled = true
                }
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                body?.let {
                    loadDataWithBaseURL(
                        apiHostUrl,
                        body.replaceLinkTags(isDarkTheme),
                        "text/html",
                        StandardCharsets.UTF_8.name(),
                        null
                    )
                }
                contentUrl?.let {
                    loadUrl(it)
                }
                applyDarkModeIfEnabled(isDarkTheme)
            }
        },
        update = { webView ->
            body?.let {
                webView.loadDataWithBaseURL(
                    apiHostUrl,
                    body.replaceLinkTags(isDarkTheme),
                    "text/html",
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
            contentUrl?.let {
                webView.loadUrl(it)
            }
        }
    )
}
