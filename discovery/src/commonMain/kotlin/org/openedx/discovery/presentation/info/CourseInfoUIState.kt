package org.openedx.discovery.presentation.info

sealed class CourseInfoUIState {
    data class CourseInfo(
        val initialUrl: String = "",
        val isPreLogin: Boolean = false,
        val enrolledCourseId: String = "",
    ) : CourseInfoUIState()
}
