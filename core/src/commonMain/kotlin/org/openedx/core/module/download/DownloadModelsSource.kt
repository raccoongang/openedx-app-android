package org.openedx.core.module.download

import kotlinx.coroutines.flow.Flow
import org.openedx.core.module.db.DownloadModel

interface DownloadModelsSource {
    fun getDownloadModelsFlow(): Flow<List<DownloadModel>>
}
