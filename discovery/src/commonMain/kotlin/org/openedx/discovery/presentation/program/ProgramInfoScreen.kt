package org.openedx.discovery.presentation.program

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.openedx.core.presentation.global.webview.WebViewUIAction
import org.openedx.core.system.AppCookieManager
import org.openedx.core.ui.FullScreenErrorView
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.discovery.*
import org.openedx.discovery.presentation.catalog.CatalogPlatformWebView
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.discovery.presentation.catalog.WebViewLink.Authority as linkAuthority

@Composable
fun ProgramInfoScreen(
    windowSize: WindowSize,
    uiState: ProgramUIState?,
    contentUrl: String,
    cookieManager: AppCookieManager,
    uriScheme: String,
    userAgent: String,
    canShowBackBtn: Boolean,
    isNestedFragment: Boolean,
    hasInternetConnection: Boolean,
    onWebViewUIAction: (WebViewUIAction) -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    onUriClick: (String, linkAuthority) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    when (uiState) {
        is ProgramUIState.UiMessage -> {
            HandleUIMessage(uiMessage = uiState.uiMessage, snackbarHostState = snackbarHostState)
        }

        else -> {}
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val modifierScreenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = if (!windowSize.isLandscape) {
                        Modifier.widthIn(Dp.Unspecified, 560.dp)
                    } else {
                        Modifier.widthIn(Dp.Unspecified, 650.dp)
                    },
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val statusBarPadding = if (isNestedFragment) {
            Modifier
        } else {
            Modifier.statusBarsInset()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(statusBarPadding)
                .displayCutoutForLandscape(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isNestedFragment) {
                Toolbar(
                    label = stringResource(Res.string.discovery_programs),
                    canShowBackBtn = canShowBackBtn,
                    canShowSettingsIcon = !canShowBackBtn,
                    onBackClick = onBackClick,
                    onSettingsClick = onSettingsClick
                )
            }

            Surface {
                Box(
                    modifier = modifierScreenWidth
                        .fillMaxHeight()
                        .background(Color.White),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if ((uiState is ProgramUIState.Error).not()) {
                        if (hasInternetConnection) {
                            CatalogPlatformWebView(
                                url = contentUrl,
                                uriScheme = uriScheme,
                                userAgent = userAgent,
                                isAllLinksExternal = true,
                                cookieManager = cookieManager,
                                onWebPageLoaded = { onWebViewUIAction(WebViewUIAction.WEB_PAGE_LOADED) },
                                onUriClick = onUriClick,
                                onWebPageLoadError = { onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR) }
                            )
                        } else {
                            onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR)
                        }
                    }

                    if (uiState is ProgramUIState.Error) {
                        FullScreenErrorView(errorType = uiState.errorType) {
                            onWebViewUIAction(WebViewUIAction.RELOAD_WEB_PAGE)
                        }
                    }

                    if (uiState == ProgramUIState.Loading && hasInternetConnection) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
