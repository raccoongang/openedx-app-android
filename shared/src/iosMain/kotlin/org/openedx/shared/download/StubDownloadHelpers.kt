package org.openedx.shared.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.DownloadCoursePreview
import org.openedx.core.module.db.DownloadModel
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogItem
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.dialog.downloaddialog.PendingDownloadDialog

/**
 * Temporary no-op for the download-dialog UI. The manager orchestrates Compose
 * popups and ties into DownloadWorkerController; wiring the full commonMain
 * DownloadDialogManagerImpl on iOS is a follow-up task.
 */
class StubDownloadDialogManager : DownloadDialogManager {
    override val pendingDialog: StateFlow<PendingDownloadDialog?> = MutableStateFlow(null)
    override fun dismissDialog() = Unit
    override fun showPopup(
        subSectionsBlocks: List<Block>,
        courseId: String,
        isBlocksDownloaded: Boolean,
        onlyVideoBlocks: Boolean,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: (blockId: String) -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) = Unit

    override fun showPopup(
        coursePreview: DownloadCoursePreview,
        isBlocksDownloaded: Boolean,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: () -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) = Unit

    override fun showRemoveDownloadModelPopup(
        downloadDialogItem: DownloadDialogItem,
        removeDownloadModels: () -> Unit,
    ) = Unit

    override fun showDownloadFailedPopup(
        downloadModel: List<DownloadModel>,
    ) = Unit
}
