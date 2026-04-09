package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class EnrolledCourse(
    val auditAccessExpires: Instant?,
    val created: String,
    val mode: String,
    val isActive: Boolean,
    val course: EnrolledCourseData,
    val certificate: Certificate?,
    val progress: Progress,
    val courseStatus: CourseStatus?,
    val courseAssignments: CourseAssignments?
)
