@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package org.openedx.shared.download

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData

class IosFileDownloader {

    private val httpClient = HttpClient(Darwin)

    var isCanceled: Boolean = false

    var progressListener: ((bytesRead: Long, total: Long) -> Unit)? = null

    private var currentHandle: NSFileHandle? = null

    private var currentPath: String? = null

    suspend fun download(url: String, path: String): DownloadResult {
        isCanceled = false
        val fileManager = NSFileManager.defaultManager
        return try {
            if (fileManager.fileExistsAtPath(path)) fileManager.removeItemAtPath(path, null)
            fileManager.createFileAtPath(path, contents = null, attributes = null)
            val handle = NSFileHandle.fileHandleForWritingAtPath(path) ?: return DownloadResult.ERROR
            currentHandle = handle
            currentPath = path

            httpClient.prepareGet(url).execute { response ->
                val channel = response.bodyAsChannel()
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
                var totalBytesRead = 0L
                val buffer = ByteArray(BUFFER_SIZE)
                while (!channel.isClosedForRead && !isCanceled) {
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead <= 0) break
                    val chunk = if (bytesRead == BUFFER_SIZE) buffer else buffer.copyOf(bytesRead)
                    handle.writeData(chunk.toNSData())
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        progressListener?.invoke(totalBytesRead, contentLength)
                    }
                }
            }
            handle.closeFile()
            currentHandle = null
            currentPath = null
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            runCatching { currentHandle?.closeFile() }
            currentHandle = null
            currentPath?.let { if (fileManager.fileExistsAtPath(it)) fileManager.removeItemAtPath(it, null) }
            currentPath = null
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.ERROR
        }
    }

    fun cancelDownloading() {
        isCanceled = true
        runCatching { currentHandle?.closeFile() }
        currentHandle = null
        currentPath?.let {
            val fm = NSFileManager.defaultManager
            if (fm.fileExistsAtPath(it)) fm.removeItemAtPath(it, null)
        }
        currentPath = null
    }

    enum class DownloadResult { SUCCESS, CANCELED, ERROR }

    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }
}

private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}
