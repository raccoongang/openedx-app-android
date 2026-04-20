package org.openedx.discovery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.presentation.global.webview.WebViewUIAction
import org.openedx.core.presentation.global.webview.WebViewUIState
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.FullScreenErrorView
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.discovery.Res
import org.openedx.discovery.discovery_explore_the_catalog
import org.openedx.discovery.presentation.catalog.CatalogPlatformWebView
import org.openedx.discovery.presentation.catalog.WebViewLink
import org.openedx.discovery.presentation.catalog.rememberWebViewBackControl
import org.openedx.core.ui.PlatformBackHandler
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun WebViewDiscoveryScreen(
    windowSize: WindowSize,
    uiState: WebViewUIState,
    isPreLogin: Boolean,
    contentUrl: String,
    uriScheme: String,
    isRegistrationEnabled: Boolean,
    userAgent: String,
    hasInternetConnection: Boolean,
    onWebViewUIAction: (WebViewUIAction) -> Unit,
    onWebPageUpdated: (String) -> Unit,
    onUriClick: (String, WebViewLink.Authority) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val backControl = rememberWebViewBackControl()
    PlatformBackHandler(enabled = backControl.canGoBack) {
        backControl.goBack()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (isPreLogin) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .navigationBarsPadding(),
                ) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled,
                    )
                }
            }
        },
    ) {
        val modifierScreenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = if (!windowSize.isLandscape) {
                        Modifier.widthIn(Dp.Unspecified, 560.dp)
                    } else {
                        Modifier.widthIn(Dp.Unspecified, 650.dp)
                    },
                    compact = Modifier.fillMaxWidth(),
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Toolbar(
                label = stringResource(Res.string.discovery_explore_the_catalog),
                canShowBackBtn = isPreLogin,
                canShowSettingsIcon = !isPreLogin,
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
            )

            Surface {
                Box(
                    modifier = modifierScreenWidth
                        .fillMaxHeight()
                        .background(Color.White),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    if (uiState !is WebViewUIState.Error) {
                        if (hasInternetConnection) {
                            CatalogPlatformWebView(
                                url = contentUrl,
                                uriScheme = uriScheme,
                                userAgent = userAgent,
                                onWebPageLoaded = {
                                    if (uiState !is WebViewUIState.Error) {
                                        onWebViewUIAction(WebViewUIAction.WEB_PAGE_LOADED)
                                    }
                                },
                                onWebPageUpdated = onWebPageUpdated,
                                onUriClick = onUriClick,
                                onWebPageLoadError = {
                                    onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR)
                                },
                                backControl = backControl,
                            )
                        } else {
                            onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR)
                        }
                    }
                    if (uiState is WebViewUIState.Error) {
                        FullScreenErrorView(errorType = uiState.errorType) {
                            onWebViewUIAction(WebViewUIAction.RELOAD_WEB_PAGE)
                        }
                    }
                    if (uiState is WebViewUIState.Loading && hasInternetConnection) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
