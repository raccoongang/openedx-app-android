package org.openedx.discovery.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Holder for in-page WebView back navigation. `canGoBack` is a Compose state updated by the
 * platform actual after each page load; `goBack()` delegates to the underlying WebView's back
 * history. Mirrors Android `WebViewDiscoveryFragment`'s `OnBackPressedCallback`.
 */
class WebViewBackControl {
    internal var canGoBackState by mutableStateOf(false)
    internal var goBackImpl: () -> Unit = {}
    val canGoBack: Boolean get() = canGoBackState
    fun goBack() = goBackImpl()
}

@Composable
fun rememberWebViewBackControl(): WebViewBackControl = remember { WebViewBackControl() }
