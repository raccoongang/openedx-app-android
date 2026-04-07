package org.openedx.downloads.presentation


interface DownloadsRouter {

    fun navigateToSettings(fm: Any?)

    fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
    )
}
