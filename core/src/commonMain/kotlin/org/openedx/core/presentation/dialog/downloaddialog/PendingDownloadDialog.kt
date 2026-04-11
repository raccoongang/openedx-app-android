package org.openedx.core.presentation.dialog.downloaddialog

/**
 * Represents a pending dialog to be shown by the Compose UI layer.
 */
data class PendingDownloadDialog(
    val dialogType: DownloadDialogType,
    val uiState: DownloadDialogUIState,
    val onConfirm: () -> Unit,
    val onDismiss: () -> Unit,
)

enum class DownloadDialogType {
    CONFIRM_DOWNLOAD,
    DOWNLOAD_ON_CELLULAR,
    REMOVE_DOWNLOAD,
    NO_CONNECTION,
    WIFI_REQUIRED,
    STORAGE_ERROR,
    DOWNLOAD_FAILED,
}
