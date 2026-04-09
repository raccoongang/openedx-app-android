package org.openedx.core.module.download

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

abstract class AbstractDownloader {

    protected abstract val httpClient: HttpClient

    private var currentDownloadingFilePath: String? = null

    var isCanceled = false

    private var fos: FileOutputStream? = null

    open suspend fun download(
        url: String,
        path: String,
    ): DownloadResult {
        isCanceled = false
        return try {
            initializeFile(path)
            httpClient.prepareGet(url).execute { response ->
                val channel = response.bodyAsChannel()
                val file = File(path)
                FileOutputStream(file).use { outputStream ->
                    fos = outputStream
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (!channel.isClosedForRead && !isCanceled) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead <= 0) break
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.ERROR
        } finally {
            closeResources()
        }
    }

    private fun initializeFile(path: String) {
        val file = File(path)
        if (file.exists()) file.delete()
        file.createNewFile()
        currentDownloadingFilePath = path
    }

    private fun closeResources() {
        fos?.close()
        fos = null
        currentDownloadingFilePath = null
    }

    suspend fun cancelDownloading() {
        isCanceled = true
        withContext(Dispatchers.IO) {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        currentDownloadingFilePath?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    enum class DownloadResult {
        SUCCESS, CANCELED, ERROR
    }

    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }
}
