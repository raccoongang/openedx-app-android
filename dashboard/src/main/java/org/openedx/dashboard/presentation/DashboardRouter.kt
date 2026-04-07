package org.openedx.dashboard.presentation

import androidx.fragment.app.Fragment

interface DashboardRouter {

    fun navigateToCourseOutline(
        fm: Any?,
        courseId: String,
        courseTitle: String,
        openTab: String,
        resumeBlockId: String
    )

    fun navigateToSettings(fm: Any?)

    fun navigateToCourseSearch(fm: Any?, querySearch: String)

    fun navigateToAllEnrolledCourses(fm: Any?)

    fun getProgramFragment(): Fragment
}
