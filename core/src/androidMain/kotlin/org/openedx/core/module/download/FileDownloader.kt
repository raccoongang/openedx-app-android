package org.openedx.core.module.download

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.FileOutputStream

class FileDownloader : AbstractDownloader() {

    var progressListener: CurrentProgress? = null

    override val httpClient: HttpClient = HttpClient(OkHttp)

    override suspend fun download(url: String, path: String): DownloadResult {
        isCanceled = false
        return try {
            val file = File(path)
            if (file.exists()) file.delete()
            file.createNewFile()

            httpClient.prepareGet(url).execute { response ->
                val channel = response.bodyAsChannel()
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
                var totalBytesRead = 0L

                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (!channel.isClosedForRead && !isCanceled) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead <= 0) break
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            progressListener?.progress(totalBytesRead, contentLength)
                            Log.d("DownloadProgress", "${100 * totalBytesRead / contentLength}% done")
                        }
                    }
                    outputStream.flush()
                }
            }
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            if (isCanceled) DownloadResult.CANCELED else DownloadResult.ERROR
        }
    }

    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }
}

interface CurrentProgress {
    fun progress(value: Long, size: Long)
}
