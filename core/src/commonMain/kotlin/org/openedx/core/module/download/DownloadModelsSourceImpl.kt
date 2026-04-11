package org.openedx.core.module.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.db.DownloadModel

class DownloadModelsSourceImpl(
    private val downloadDao: DownloadDao,
) : DownloadModelsSource {
    override fun getDownloadModelsFlow(): Flow<List<DownloadModel>> {
        return downloadDao.getAllDataFlow().map { list -> list.map { it.mapToDomain() } }
    }
}
