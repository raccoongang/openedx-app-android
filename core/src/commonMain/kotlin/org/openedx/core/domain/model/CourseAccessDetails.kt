package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class CourseAccessDetails(
    val hasUnmetPrerequisites: Boolean,
    val isTooEarly: Boolean,
    val isStaff: Boolean,
    val auditAccessExpires: Instant?,
    val coursewareAccess: CoursewareAccess?,
)
