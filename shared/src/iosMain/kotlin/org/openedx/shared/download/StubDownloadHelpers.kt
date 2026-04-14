package org.openedx.shared.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.DownloadCoursePreview
import org.openedx.core.module.db.DownloadModel
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.module.download.DownloadModelsSource
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogItem
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.dialog.downloaddialog.PendingDownloadDialog

/**
 * iOS no-op implementations for the Download helper trio.
 *
 * TODO iOS: when downloads are actually wired (Phase 7 stub `IosDownloadWorkerController`
 * is no-op too), implement these against URLSession + Files in NSCachesDirectory.
 */

class StubDownloadDialogManager : DownloadDialogManager {
    override val pendingDialog: StateFlow<PendingDownloadDialog?> = MutableStateFlow(null)
    override fun dismissDialog() = Unit
    override fun showPopup(
        subSectionsBlocks: List<Block>,
        courseId: String,
        isBlocksDownloaded: Boolean,
        onlyVideoBlocks: Boolean,
        fragmentManager: Any?,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: (blockId: String) -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) = Unit

    override fun showPopup(
        coursePreview: DownloadCoursePreview,
        isBlocksDownloaded: Boolean,
        fragmentManager: Any?,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: () -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) = Unit

    override fun showRemoveDownloadModelPopup(
        downloadDialogItem: DownloadDialogItem,
        fragmentManager: Any?,
        removeDownloadModels: () -> Unit,
    ) = Unit

    override fun showDownloadFailedPopup(
        downloadModel: List<DownloadModel>,
        fragmentManager: Any?,
    ) = Unit
}

class StubDownloadModelsSource : DownloadModelsSource {
    override fun getDownloadModelsFlow() = flowOf(emptyList<DownloadModel>())
}

class StubDownloadHelper : DownloadHelper {
    override fun generateDownloadModelFromBlock(folder: String, block: Block, courseId: String): DownloadModel? = null
    override suspend fun updateDownloadStatus(downloadModel: DownloadModel): DownloadModel? = null
}
