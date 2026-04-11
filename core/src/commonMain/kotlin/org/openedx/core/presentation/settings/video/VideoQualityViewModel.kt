package org.openedx.core.presentation.settings.video

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.VideoQuality
import org.openedx.core.presentation.CoreAnalytics
import org.openedx.core.presentation.CoreAnalyticsEvent
import org.openedx.core.presentation.CoreAnalyticsKey
import org.openedx.core.system.notifier.VideoNotifier
import org.openedx.core.system.notifier.VideoQualityChanged
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager

class VideoQualityViewModel(
    private val qualityType: String,
    private val preferencesManager: CorePreferences,
    private val notifier: VideoNotifier,
    private val analytics: CoreAnalytics,
    private val resourceManager: ResourceManager,
) : BaseViewModel() {

    private val _videoQuality = MutableStateFlow(getCurrentVideoQuality())
    val videoQuality: StateFlow<VideoQuality> = _videoQuality.asStateFlow()

    fun getCurrentVideoQuality(): VideoQuality {
        return if (getQualityType() == VideoQualityType.Streaming) {
            preferencesManager.videoSettings.videoStreamingQuality
        } else {
            preferencesManager.videoSettings.videoDownloadQuality
        }
    }

    fun setVideoQuality(quality: VideoQuality) {
        val currentSettings = preferencesManager.videoSettings
        logVideoQualityChangedEvent(getCurrentVideoQuality(), quality)
        if (getQualityType() == VideoQualityType.Streaming) {
            preferencesManager.videoSettings = currentSettings.copy(videoStreamingQuality = quality)
        } else {
            preferencesManager.videoSettings = currentSettings.copy(videoDownloadQuality = quality)
        }
        _videoQuality.value = getCurrentVideoQuality()
        viewModelScope.launch {
            notifier.send(VideoQualityChanged())
        }
    }

    fun getQualityType() = VideoQualityType.valueOf(qualityType)

    private fun logVideoQualityChangedEvent(oldQuality: VideoQuality, newQuality: VideoQuality) {
        val event = if (getQualityType() == VideoQualityType.Streaming) {
                CoreAnalyticsEvent.VIDEO_STREAMING_QUALITY_CHANGED
        } else {
                CoreAnalyticsEvent.VIDEO_DOWNLOAD_QUALITY_CHANGED
        }

        analytics.logEvent(
            event.eventName,
            mapOf(
                CoreAnalyticsKey.NAME.key to event.biValue,
                CoreAnalyticsKey.CATEGORY.key to CoreAnalyticsKey.PROFILE.key,
                CoreAnalyticsKey.VALUE.key to newQuality.tagId,
                CoreAnalyticsKey.OLD_VALUE.key to oldQuality.tagId,
            )
        )
    }
}
