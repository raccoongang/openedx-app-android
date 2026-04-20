package org.openedx.core.presentation.dialog.downloaddialog

import kotlinx.coroutines.flow.StateFlow
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.DownloadCoursePreview
import org.openedx.core.module.db.DownloadModel

interface DownloadDialogManager {
    companion object {
        const val MAX_CELLULAR_SIZE = 104857600 // 100MB
        const val DOWNLOAD_SIZE_FACTOR = 2
    }

    val pendingDialog: StateFlow<PendingDownloadDialog?>
    fun dismissDialog()
    fun showPopup(
        subSectionsBlocks: List<Block>,
        courseId: String,
        isBlocksDownloaded: Boolean,
        onlyVideoBlocks: Boolean = false,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: (blockId: String) -> Unit,
        onDismissClick: () -> Unit = {},
        onConfirmClick: () -> Unit = {},
    )
    fun showPopup(
        coursePreview: DownloadCoursePreview,
        isBlocksDownloaded: Boolean,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: () -> Unit,
        onDismissClick: () -> Unit = {},
        onConfirmClick: () -> Unit = {},
    )
    fun showRemoveDownloadModelPopup(
        downloadDialogItem: DownloadDialogItem,
        removeDownloadModels: () -> Unit,
    )
    fun showDownloadFailedPopup(
        downloadModel: List<DownloadModel>,
    )
}
