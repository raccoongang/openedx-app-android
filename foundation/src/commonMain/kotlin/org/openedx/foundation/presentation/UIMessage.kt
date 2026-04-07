package org.openedx.foundation.presentation

import androidx.compose.material3.SnackbarDuration

open class UIMessage(open val message: String = "") {
    data class SnackBarMessage(
        override val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Long,
    ) : UIMessage(message)

    data class ToastMessage(
        override val message: String,
    ) : UIMessage(message)
}
