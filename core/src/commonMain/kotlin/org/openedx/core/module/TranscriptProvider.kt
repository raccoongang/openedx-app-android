package org.openedx.core.module

data class TranscriptResult(
    val transcriptObject: Any?,
    val timeList: List<Long>,
)

interface TranscriptProvider {
    suspend fun downloadTranscripts(url: String): TranscriptResult?
    suspend fun cancelDownloading()
}
