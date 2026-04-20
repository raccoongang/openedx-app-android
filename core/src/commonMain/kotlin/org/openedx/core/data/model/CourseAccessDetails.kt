package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.utils.InstantUtils
import org.openedx.core.domain.model.CourseAccessDetails as DomainCourseAccessDetails

@Serializable
data class CourseAccessDetails(
    @SerialName("has_unmet_prerequisites")
    val hasUnmetPrerequisites: Boolean,
    @SerialName("is_too_early")
    val isTooEarly: Boolean,
    @SerialName("is_staff")
    val isStaff: Boolean,
    @SerialName("audit_access_expires")
    val auditAccessExpires: String? = null,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess? = null,
) {
    fun mapToDomain() = DomainCourseAccessDetails(
        hasUnmetPrerequisites = hasUnmetPrerequisites,
        isTooEarly = isTooEarly,
        isStaff = isStaff,
        auditAccessExpires = InstantUtils.iso8601ToInstant(auditAccessExpires ?: ""),
        coursewareAccess = coursewareAccess?.mapToDomain(),
    )
}
