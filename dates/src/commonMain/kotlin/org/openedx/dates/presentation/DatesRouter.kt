package org.openedx.dates.presentation


interface DatesRouter {

    fun navigateToSettings(fm: Any?)

    fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
        openTab: String,
        resumeBlockId: String
    )
}
