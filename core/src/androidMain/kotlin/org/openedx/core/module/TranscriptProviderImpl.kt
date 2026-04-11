package org.openedx.core.module

class TranscriptProviderImpl(
    private val transcriptManager: TranscriptManager,
) : TranscriptProvider {

    override suspend fun downloadTranscripts(url: String): TranscriptResult? {
        val timedTextObject = transcriptManager.downloadTranscriptsForVideo(url) ?: return null
        val timeList = timedTextObject.captions?.values?.toList()
            ?.map { it.start.mseconds.toLong() } ?: emptyList()
        return TranscriptResult(
            transcriptObject = timedTextObject,
            timeList = timeList,
        )
    }

    override suspend fun cancelDownloading() {
        transcriptManager.cancelTranscriptDownloading()
    }
}
