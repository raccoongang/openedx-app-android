package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import org.openedx.core.utils.TimeUtils
import org.openedx.core.domain.model.CourseAccessDetails as DomainCourseAccessDetails

data class CourseAccessDetails(
    @SerialName("has_unmet_prerequisites")
    val hasUnmetPrerequisites: Boolean,
    @SerialName("is_too_early")
    val isTooEarly: Boolean,
    @SerialName("is_staff")
    val isStaff: Boolean,
    @SerialName("audit_access_expires")
    val auditAccessExpires: String?,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess?,
) {
    fun mapToDomain() = DomainCourseAccessDetails(
        hasUnmetPrerequisites = hasUnmetPrerequisites,
        isTooEarly = isTooEarly,
        isStaff = isStaff,
        auditAccessExpires = TimeUtils.iso8601ToDate(auditAccessExpires ?: ""),
        coursewareAccess = coursewareAccess?.mapToDomain(),
    )

    fun mapToRoomEntity(): CourseAccessDetailsDb =
        CourseAccessDetailsDb(
            hasUnmetPrerequisites = hasUnmetPrerequisites,
            isTooEarly = isTooEarly,
            isStaff = isStaff,
            auditAccessExpires = auditAccessExpires,
            coursewareAccess = coursewareAccess?.mapToRoomEntity()
        )
}
