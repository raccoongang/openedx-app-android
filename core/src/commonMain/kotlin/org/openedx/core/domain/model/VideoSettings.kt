package org.openedx.core.domain.model

data class VideoSettings(
    val wifiDownloadOnly: Boolean,
    val videoStreamingQuality: VideoQuality,
    val videoDownloadQuality: VideoQuality,
) {
    companion object {
        val default = VideoSettings(true, VideoQuality.AUTO, VideoQuality.AUTO)
    }
}

enum class VideoQuality(
    val width: Int,
    val height: Int,
    val tagId: String = "",
) {
    AUTO(width = 0, height = 0, tagId = "auto"),
    OPTION_360P(width = 640, height = 360, tagId = "low"),
    OPTION_540P(width = 960, height = 540, tagId = "medium"),
    OPTION_720P(width = 1280, height = 720, tagId = "high"),
}
