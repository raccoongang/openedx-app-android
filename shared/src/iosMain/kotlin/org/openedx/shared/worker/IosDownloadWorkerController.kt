package org.openedx.shared.worker

import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.db.DownloadModel

/**
 * iOS stub for DownloadWorkerController.
 * TODO: Implement using NSURLSession background downloads.
 */
class IosDownloadWorkerController : DownloadWorkerController {
    override suspend fun saveModels(downloadModels: List<DownloadModel>) {
        // No-op: iOS download not yet implemented
    }

    override suspend fun removeModel(id: String) {
        // No-op
    }

    override suspend fun removeModels(ids: List<String>) {
        // No-op
    }

    override suspend fun removeModels() {
        // No-op
    }
}
