package org.openedx.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import org.openedx.foundation.presentation.UIMessage

@Composable
@NonRestartableComposable
actual fun HandleUIMessage(
    uiMessage: UIMessage?,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(uiMessage) {
        when (uiMessage) {
            is UIMessage.SnackBarMessage -> {
                snackbarHostState.showSnackbar(
                    message = uiMessage.message,
                    duration = uiMessage.duration
                )
            }
            else -> {}
        }
    }
}
