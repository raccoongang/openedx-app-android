package org.openedx.core.domain.model

import org.openedx.core.extension.isNotNull
import java.util.Date

data class CourseEnrollmentDetails(
    val id: String,
    val courseUpdates: String,
    val courseHandouts: String,
    val discussionUrl: String,
    val courseAccessDetails: CourseAccessDetails,
    val certificate: Certificate?,
    val enrollmentDetails: EnrollmentDetails,
    val courseInfoOverview: CourseInfoOverview,
) {

    val hasAccess: Boolean
        get() = courseAccessDetails.coursewareAccess?.hasAccess ?: false

    val isAuditAccessExpired: Boolean
        get() = courseAccessDetails.auditAccessExpires.isNotNull() &&
                Date().after(courseAccessDetails.auditAccessExpires)
}

enum class CourseAccessError {
    NONE, AUDIT_EXPIRED_NOT_UPGRADABLE, NOT_YET_STARTED, UNKNOWN
}
