@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.openedx.shared.worker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.db.DownloadModel
import org.openedx.core.module.db.DownloadModelEntity
import org.openedx.core.module.db.DownloadedState
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.system.notifier.DownloadFailed
import org.openedx.core.system.notifier.DownloadNotifier
import org.openedx.core.system.notifier.DownloadProgressChanged
import org.openedx.shared.download.IosFileDownloader
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.timeIntervalSince1970

class IosDownloadWorkerController(
    private val downloadDao: DownloadDao,
    private val downloadHelper: DownloadHelper,
    private val fileDownloader: IosFileDownloader,
    private val downloadNotifier: DownloadNotifier,
) : DownloadWorkerController {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var workerJob: Job? = null
    private val downloadError = mutableListOf<DownloadModel>()
    private var lastUpdateTimeMs = 0L

    init {
        scope.launch {
            downloadDao.getAllDataFlow().collect { list ->
                val domain = list.map { it.mapToDomain() }
                if (domain.any { it.downloadedState == DownloadedState.WAITING }) {
                    ensureWorker()
                }
            }
        }
    }

    private fun ensureWorker() {
        if (workerJob?.isActive == true) return
        workerJob = scope.launch { runQueue() }
    }

    private suspend fun runQueue() {
        try {
            while (true) {
                val next = downloadDao.getAllDataFlow().first()
                    .map { it.mapToDomain() }
                    .firstOrNull { it.downloadedState == DownloadedState.WAITING }
                    ?: break
                runSingleDownload(next)
            }
            if (downloadError.isNotEmpty()) {
                downloadNotifier.send(DownloadFailed(downloadError.toList()))
                downloadError.clear()
            }
        } finally {
            fileDownloader.progressListener = null
        }
    }

    private suspend fun runSingleDownload(task: DownloadModel) {
        fileDownloader.progressListener = { bytes, total ->
            val now = nowMs()
            if (total > 0 && now - lastUpdateTimeMs > PROGRESS_UPDATE_INTERVAL_MS) {
                lastUpdateTimeMs = now
                scope.launch { downloadNotifier.send(DownloadProgressChanged(task.id, bytes, total)) }
            }
        }
        downloadDao.updateDownloadModel(
            DownloadModelEntity.createFrom(task.copy(downloadedState = DownloadedState.DOWNLOADING))
        )
        val result = fileDownloader.download(task.url, task.path)
        when (result) {
            IosFileDownloader.DownloadResult.SUCCESS -> {
                val updated = downloadHelper.updateDownloadStatus(task)
                if (updated != null) {
                    downloadDao.updateDownloadModel(DownloadModelEntity.createFrom(updated))
                } else {
                    downloadDao.removeDownloadModel(task.id)
                    downloadError.add(task)
                }
            }
            IosFileDownloader.DownloadResult.CANCELED -> {
                downloadDao.removeDownloadModel(task.id)
            }
            IosFileDownloader.DownloadResult.ERROR -> {
                downloadDao.removeDownloadModel(task.id)
                downloadError.add(task)
            }
        }
    }

    override suspend fun saveModels(downloadModels: List<DownloadModel>) {
        downloadDao.insertDownloadModel(downloadModels.map { DownloadModelEntity.createFrom(it) })
    }

    override suspend fun removeModel(id: String) {
        removeModels(listOf(id))
    }

    override suspend fun removeModels(ids: List<String>) {
        val models = downloadDao.readAllDataByIds(ids).first().map { it.mapToDomain() }
        val removeIds = mutableListOf<String>()
        var hasDownloading = false
        val fm = NSFileManager.defaultManager
        models.forEach { m ->
            removeIds.add(m.id)
            if (m.downloadedState == DownloadedState.DOWNLOADING) hasDownloading = true
            runCatching {
                if (fm.fileExistsAtPath(m.path)) fm.removeItemAtPath(m.path, null)
            }
        }
        if (hasDownloading) fileDownloader.cancelDownloading()
        downloadDao.removeAllDownloadModels(removeIds)
        downloadDao.removeOfflineXBlockProgress(removeIds)

        val remaining = downloadDao.getAllDataFlow().first()
            .map { it.mapToDomain() }
            .any { it.downloadedState.isWaitingOrDownloading }
        if (!remaining) {
            workerJob?.cancel()
            workerJob = null
        }
    }

    override suspend fun removeModels() {
        fileDownloader.cancelDownloading()
        workerJob?.cancel()
        workerJob = null
    }

    private fun nowMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 200L
    }
}
