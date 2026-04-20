package org.openedx.core.presentation.dialog.downloaddialog

data class DownloadDialogUIState(
    val downloadDialogItems: List<DownloadDialogItem> = emptyList(),
    val sizeSum: Long,
    val isAllBlocksDownloaded: Boolean,
    val isDownloadFailed: Boolean,
    val removeDownloadModels: () -> Unit,
    val saveDownloadModels: () -> Unit,
    val onDismissClick: () -> Unit = {},
    val onConfirmClick: () -> Unit = {},
)
