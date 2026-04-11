package org.openedx.core.presentation.dialog.downloaddialog

import androidx.compose.ui.graphics.vector.ImageVector

data class DownloadDialogItem(
    val title: String,
    val size: Long,
    val icon: ImageVector? = null
)
