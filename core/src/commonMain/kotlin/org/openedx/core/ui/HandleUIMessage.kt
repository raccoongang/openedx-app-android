package org.openedx.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.openedx.foundation.presentation.UIMessage

@Composable
expect fun HandleUIMessage(
    uiMessage: UIMessage?,
    snackbarHostState: SnackbarHostState,
)
