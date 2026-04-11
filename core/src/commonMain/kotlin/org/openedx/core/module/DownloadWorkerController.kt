package org.openedx.core.module

import org.openedx.core.module.db.DownloadModel

interface DownloadWorkerController {
    suspend fun saveModels(downloadModels: List<DownloadModel>)
    suspend fun removeModel(id: String)
    suspend fun removeModels(ids: List<String>)
    suspend fun removeModels()
}
