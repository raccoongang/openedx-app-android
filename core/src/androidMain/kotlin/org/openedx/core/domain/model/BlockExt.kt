package org.openedx.core.domain.model

import android.content.Context
import org.openedx.core.utils.PreviewHelper
import org.openedx.core.utils.VideoPreview

fun Block.getVideoPreview(context: Context, isOnline: Boolean, offlineUrl: String?): VideoPreview? {
    return if (studentViewData?.encodedVideos?.hasYoutubeUrl == true) {
        val youtubeUrl = studentViewData.encodedVideos.youtube?.url ?: ""
        VideoPreview.createYoutubePreview(
            PreviewHelper.getYouTubeThumbnailUrl(youtubeUrl)
        )
    } else if (studentViewData?.encodedVideos?.hasVideoUrl == true) {
        val videoUrl = if (studentViewData.encodedVideos.videoUrl.isNotEmpty() && isOnline) {
            studentViewData.encodedVideos.videoUrl
        } else {
            offlineUrl ?: ""
        }
        val bitmap = PreviewHelper.getVideoFrameBitmap(
            context = context,
            isOnline = isOnline,
            videoUrl = videoUrl
        )
        bitmap?.let { VideoPreview.createEncodedVideoPreview(it) }
    } else {
        null
    }
}
