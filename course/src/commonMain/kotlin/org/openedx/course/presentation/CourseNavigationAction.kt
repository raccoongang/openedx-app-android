package org.openedx.course.presentation

import org.openedx.course.presentation.unit.container.CourseViewMode

sealed class CourseNavigationAction {
    data class NavigateToCourseContainer(
        val courseId: String,
        val unitId: String,
        val componentId: String = "",
        val mode: CourseViewMode,
    ) : CourseNavigationAction()

    data class NavigateToCourseSubsections(
        val courseId: String,
        val subSectionId: String,
        val unitId: String = "",
        val componentId: String = "",
        val mode: CourseViewMode,
    ) : CourseNavigationAction()

    data class NavigateToDownloadQueue(
        val descendants: List<String>,
    ) : CourseNavigationAction()
}
