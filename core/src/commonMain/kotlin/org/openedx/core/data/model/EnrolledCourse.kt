package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.utils.InstantUtils
import org.openedx.core.domain.model.Progress as ProgressDomain

@Serializable
data class EnrolledCourse(
    @SerialName("audit_access_expires")
    val auditAccessExpires: String?,
    @SerialName("created")
    val created: String?,
    @SerialName("mode")
    val mode: String?,
    @SerialName("is_active")
    val isActive: Boolean?,
    @SerialName("course")
    val course: EnrolledCourseData?,
    @SerialName("certificate")
    val certificate: Certificate?,
    @SerialName("course_progress")
    val progress: Progress?,
    @SerialName("course_status")
    val courseStatus: CourseStatus?,
    @SerialName("course_assignments")
    val courseAssignments: CourseAssignments?
) {
    fun mapToDomain(): EnrolledCourse {
        return EnrolledCourse(
            auditAccessExpires = InstantUtils.iso8601ToInstant(auditAccessExpires ?: ""),
            created = created ?: "",
            mode = mode ?: "",
            isActive = isActive ?: false,
            course = course?.mapToDomain()!!,
            certificate = certificate?.mapToDomain(),
            progress = progress?.mapToDomain() ?: ProgressDomain.DEFAULT_PROGRESS,
            courseStatus = courseStatus?.mapToDomain(),
            courseAssignments = courseAssignments?.mapToDomain()
        )
    }
}
