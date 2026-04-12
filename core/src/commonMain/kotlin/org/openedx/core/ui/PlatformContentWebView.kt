package org.openedx.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformContentWebView(
    modifier: Modifier = Modifier,
    apiHostUrl: String? = null,
    body: String? = null,
    contentUrl: String? = null,
    onWebPageLoaded: () -> Unit = {},
)
