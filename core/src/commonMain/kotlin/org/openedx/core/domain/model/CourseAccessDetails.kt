package org.openedx.core.domain.model

import java.util.Date

data class CourseAccessDetails(
    val hasUnmetPrerequisites: Boolean,
    val isTooEarly: Boolean,
    val isStaff: Boolean,
    val auditAccessExpires: Date?,
    val coursewareAccess: CoursewareAccess?,
)
