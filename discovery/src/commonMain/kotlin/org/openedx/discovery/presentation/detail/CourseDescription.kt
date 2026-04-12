package org.openedx.discovery.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CourseDescription(
    modifier: Modifier,
    apiHostUrl: String,
    body: String,
    onWebPageLoaded: () -> Unit,
)
