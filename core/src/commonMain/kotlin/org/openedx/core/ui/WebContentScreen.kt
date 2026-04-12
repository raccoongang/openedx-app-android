package org.openedx.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.openedx.core.ui.theme.appColors
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun WebContentScreen(
    windowSize: WindowSize,
    apiHostUrl: String? = null,
    title: String,
    onBackClick: () -> Unit,
    htmlBody: String? = null,
    contentUrl: String? = null,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        containerColor = MaterialTheme.appColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(screenWidth) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Toolbar(
                        label = title,
                        canShowBackBtn = true,
                        onBackClick = onBackClick
                    )
                }
                Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.appColors.background
                ) {
                    if (htmlBody.isNullOrEmpty() && contentUrl.isNullOrEmpty()) {
                        CircularProgress()
                    } else {
                        var webViewAlpha by rememberSaveable { mutableFloatStateOf(0f) }
                        Surface(
                            Modifier.alpha(webViewAlpha),
                            color = MaterialTheme.appColors.background
                        ) {
                            PlatformContentWebView(
                                apiHostUrl = apiHostUrl,
                                body = htmlBody,
                                contentUrl = contentUrl,
                                onWebPageLoaded = {
                                    webViewAlpha = 1f
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
