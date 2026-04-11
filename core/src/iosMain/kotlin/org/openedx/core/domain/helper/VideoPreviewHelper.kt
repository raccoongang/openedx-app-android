package org.openedx.core.domain.helper

import org.openedx.core.domain.model.Block
import org.openedx.core.utils.VideoPreview

/**
 * iOS implementation of VideoPreviewHelper.
 * Returns YouTube thumbnail URLs where available; encoded video previews are not yet supported.
 */
actual class VideoPreviewHelper {

    actual fun getVideoPreview(block: Block, offlineUrl: String?): VideoPreview? {
        // On iOS, return YouTube thumbnail if available
        if (block.studentViewData?.encodedVideos?.hasYoutubeUrl == true) {
            val youtubeUrl = block.studentViewData?.encodedVideos?.youtube?.url ?: ""
            return VideoPreview.createYoutubePreview(getYouTubeThumbnailUrl(youtubeUrl))
        }
        // Encoded video frame extraction not yet implemented on iOS
        return null
    }

    actual fun getVideoPreviews(
        blocks: List<Block>,
        offlineUrls: Map<String, String>?,
    ): Map<String, VideoPreview?> {
        return blocks.associate { block ->
            val offlineUrl = offlineUrls?.get(block.id)
            block.id to getVideoPreview(block, offlineUrl)
        }
    }

    actual fun getVideoPreviewWithId(
        blockId: String,
        block: Block,
        offlineUrl: String?,
    ): Pair<String, VideoPreview?> {
        return blockId to getVideoPreview(block, offlineUrl)
    }

    private fun getYouTubeThumbnailUrl(videoUrl: String): String {
        val videoId = extractYouTubeVideoId(videoUrl) ?: return ""
        return "https://img.youtube.com/vi/$videoId/0.jpg"
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            Regex("(?:v=|/v/|youtu\\.be/)([a-zA-Z0-9_-]{11})"),
        )
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
}
