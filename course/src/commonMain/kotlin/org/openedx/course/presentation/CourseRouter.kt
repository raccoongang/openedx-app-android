package org.openedx.course.presentation

import org.openedx.course.presentation.handouts.HandoutsType
import org.openedx.course.presentation.unit.container.CourseViewMode

interface CourseRouter {

    fun navigateToNoAccess(
        fm: Any?,
        title: String
    )

    fun navigateToCourseSubsections(
        fm: Any?,
        courseId: String,
        subSectionId: String,
        unitId: String = "",
        componentId: String = "",
        mode: CourseViewMode
    )

    fun navigateToCourseContainer(
        fm: Any?,
        courseId: String,
        unitId: String,
        componentId: String = "",
        mode: CourseViewMode
    )

    fun replaceCourseContainer(
        fm: Any?,
        courseId: String,
        unitId: String,
        componentId: String = "",
        mode: CourseViewMode
    )

    fun navigateToFullScreenVideo(
        fm: Any?,
        videoUrl: String,
        videoTime: Long,
        blockId: String,
        courseId: String,
        isPlaying: Boolean
    )

    fun navigateToFullScreenYoutubeVideo(
        fm: Any?,
        videoUrl: String,
        videoTime: Long,
        blockId: String,
        courseId: String,
        isPlaying: Boolean
    )

    fun navigateToHandoutsWebView(
        fm: Any?,
        courseId: String,
        type: HandoutsType
    )

    fun navigateToDownloadQueue(fm: Any?, descendants: List<String> = arrayListOf())

    fun navigateToDiscover(fm: Any?)
}
