package org.openedx.core.domain.model

import java.util.Date

data class EnrolledCourse(
    val auditAccessExpires: Date?,
    val created: String,
    val mode: String,
    val isActive: Boolean,
    val course: EnrolledCourseData,
    val certificate: Certificate?,
    val progress: Progress,
    val courseStatus: CourseStatus?,
    val courseAssignments: CourseAssignments?
)
