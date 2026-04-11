package org.openedx.core.utils

data class VideoPreview(
    val link: String? = null,
    val platformBitmap: Any? = null,
) {
    companion object {
        fun createYoutubePreview(link: String): VideoPreview {
            return VideoPreview(link = link)
        }

        fun createEncodedVideoPreview(platformBitmap: Any): VideoPreview {
            return VideoPreview(platformBitmap = platformBitmap)
        }
    }
}
