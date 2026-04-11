package org.openedx.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.platform.LocalContext
import org.openedx.foundation.extension.toastMessage
import org.openedx.foundation.presentation.UIMessage

@Composable
@NonRestartableComposable
actual fun HandleUIMessage(
    uiMessage: UIMessage?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(uiMessage) {
        when (uiMessage) {
            is UIMessage.SnackBarMessage -> {
                snackbarHostState.showSnackbar(
                    message = uiMessage.message,
                    duration = uiMessage.duration
                )
            }

            is UIMessage.ToastMessage -> {
                context.toastMessage(uiMessage.message)
            }

            else -> {}
        }
    }
}
