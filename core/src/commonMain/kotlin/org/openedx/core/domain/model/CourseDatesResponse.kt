package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class CourseDatesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<CourseDate>
)

data class CourseDate(
    val courseId: String,
    val firstComponentBlockId: String,
    val dueDate: Instant,
    val assignmentTitle: String,
    val learnerHasAccess: Boolean,
    val relative: Boolean,
    val courseName: String
)
