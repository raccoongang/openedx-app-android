package org.openedx.course.presentation.unit.video

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.CourseVideoPositionChanged
import org.openedx.course.data.repository.CourseRepository
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.foundation.system.ResourceManager

/**
 * Equivalent of androidx.media3.common.C.TIME_UNSET (Long.MIN_VALUE + 1).
 * Used to indicate an unset or unknown time value.
 */
private const val TIME_UNSET = Long.MIN_VALUE + 1

class VideoViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository,
    private val notifier: CourseNotifier,
    private val preferencesManager: CorePreferences,
    courseAnalytics: CourseAnalytics,
    resourceManager: ResourceManager,
) : BaseVideoViewModel(courseId, courseAnalytics, resourceManager) {

    var videoUrl = ""
    var currentVideoTime = 0L
    var duration = 0L
    var isPlaying: Boolean? = null

    private var isBlockAlreadyCompleted = false

    fun sendTime() {
        if (currentVideoTime != TIME_UNSET) {
            viewModelScope.launch {
                notifier.send(
                    CourseVideoPositionChanged(
                        videoUrl,
                        currentVideoTime,
                        duration,
                        isPlaying == true
                    )
                )
            }
        }
    }

    fun markBlockCompleted(blockId: String, medium: String) {
        if (!isBlockAlreadyCompleted) {
            logLoadedCompletedEvent(videoUrl, false, currentVideoTime, medium)
            viewModelScope.launch {
                try {
                    isBlockAlreadyCompleted = true
                    courseRepository.markBlocksCompletion(
                        courseId,
                        listOf(blockId)
                    )
                    notifier.send(CourseCompletionSet())
                } catch (e: Exception) {
                    e.printStackTrace()
                    isBlockAlreadyCompleted = false
                }
            }
        }
    }

    fun getVideoQuality() = preferencesManager.videoSettings.videoStreamingQuality
}
