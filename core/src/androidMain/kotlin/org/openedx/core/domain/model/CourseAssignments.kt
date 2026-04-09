package org.openedx.core.domain.model


data class CourseAssignments(
    val futureAssignments: List<CourseDateBlock>?,
    val pastAssignments: List<CourseDateBlock>?
)
