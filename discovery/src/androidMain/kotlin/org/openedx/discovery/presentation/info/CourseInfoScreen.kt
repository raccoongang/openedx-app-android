package org.openedx.discovery.presentation.info

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.platform.LocalConfiguration
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import org.openedx.core.presentation.global.webview.WebViewUIAction
import org.openedx.core.presentation.global.webview.WebViewUIState
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.FullScreenErrorView
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.discovery.*
import org.openedx.discovery.presentation.catalog.CatalogWebViewScreen
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.discovery.presentation.catalog.WebViewLink.Authority as linkAuthority

@Composable
fun CourseInfoScreen(
    windowSize: WindowSize,
    uiState: CourseInfoUIState,
    webViewUIState: WebViewUIState,
    uiMessage: UIMessage?,
    uriScheme: String,
    isRegistrationEnabled: Boolean,
    userAgent: String,
    hasInternetConnection: Boolean,
    onWebViewUIAction: (WebViewUIAction) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
    onUriClick: (String, linkAuthority) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }

    HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.appColors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if ((uiState as CourseInfoUIState.CourseInfo).isPreLogin) {
                Box(
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp,
                            vertical = 32.dp,
                        )
                ) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled
                    )
                }
            }
        }
    ) {
        val modifierScreenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Modifier.widthIn(Dp.Unspecified, 560.dp)
                    } else {
                        Modifier.widthIn(Dp.Unspecified, 650.dp)
                    },
                    compact = Modifier.fillMaxWidth()
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
                label = stringResource(Res.string.discovery_Discovery),
                canShowBackBtn = true,
                onBackClick = onBackClick
            )

            Surface {
                Box(
                    modifier = modifierScreenWidth
                        .fillMaxHeight()
                        .background(Color.White)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if ((webViewUIState is WebViewUIState.Error).not()) {
                        if (hasInternetConnection) {
                            CourseInfoWebView(
                                contentUrl = (uiState as CourseInfoUIState.CourseInfo).initialUrl,
                                uriScheme = uriScheme,
                                userAgent = userAgent,
                                onWebPageLoaded = { onWebViewUIAction(WebViewUIAction.WEB_PAGE_LOADED) },
                                onUriClick = onUriClick,
                                onWebPageLoadError = {
                                    onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR)
                                }
                            )
                        } else {
                            onWebViewUIAction(WebViewUIAction.WEB_PAGE_ERROR)
                        }
                    }
                    if (webViewUIState is WebViewUIState.Error) {
                        FullScreenErrorView(errorType = webViewUIState.errorType) {
                            onWebViewUIAction(WebViewUIAction.RELOAD_WEB_PAGE)
                        }
                    }
                    if (webViewUIState is WebViewUIState.Loading && hasInternetConnection) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@SuppressLint("SetJavaScriptEnabled")
fun CourseInfoWebView(
    contentUrl: String,
    uriScheme: String,
    userAgent: String,
    onWebPageLoaded: () -> Unit,
    onUriClick: (String, linkAuthority) -> Unit,
    onWebPageLoadError: () -> Unit
) {
    val webView = CatalogWebViewScreen(
        url = contentUrl,
        uriScheme = uriScheme,
        userAgent = userAgent,
        isAllLinksExternal = true,
        onWebPageLoaded = onWebPageLoaded,
        onUriClick = onUriClick,
        onWebPageLoadError = onWebPageLoadError
    )

    AndroidView(
        modifier = Modifier
            .background(MaterialTheme.appColors.background),
        factory = {
            webView
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CourseInfoScreenPreview() {
    OpenEdXTheme {
        CourseInfoScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseInfoUIState.CourseInfo(
                initialUrl = "https://www.example.com/",
                isPreLogin = false,
                enrolledCourseId = ""
            ),
            uiMessage = null,
            uriScheme = "",
            isRegistrationEnabled = true,
            userAgent = "",
            hasInternetConnection = false,
            onWebViewUIAction = {},
            onRegisterClick = {},
            onSignInClick = {},
            onBackClick = {},
            onUriClick = { _, _ -> },
            webViewUIState = WebViewUIState.Loading,
        )
    }
}
