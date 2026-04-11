package org.openedx.core.module.download

import org.openedx.core.domain.model.Block
import org.openedx.core.module.db.DownloadModel

interface DownloadHelper {
    fun generateDownloadModelFromBlock(folder: String, block: Block, courseId: String): DownloadModel?
    suspend fun updateDownloadStatus(downloadModel: DownloadModel): DownloadModel?
}
