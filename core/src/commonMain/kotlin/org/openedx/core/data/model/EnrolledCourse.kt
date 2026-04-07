package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.room.discovery.EnrolledCourseEntity
import org.openedx.core.data.model.room.discovery.ProgressDb
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.utils.TimeUtils
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
            auditAccessExpires = TimeUtils.iso8601ToDate(auditAccessExpires ?: ""),
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

    fun mapToRoomEntity(): EnrolledCourseEntity {
        return EnrolledCourseEntity(
            courseId = course?.id ?: "",
            auditAccessExpires = auditAccessExpires ?: "",
            created = created ?: "",
            mode = mode ?: "",
            isActive = isActive ?: false,
            course = course?.mapToRoomEntity()!!,
            certificate = certificate?.mapToRoomEntity(),
            progress = progress?.mapToRoomEntity() ?: ProgressDb.DEFAULT_PROGRESS,
            courseStatus = courseStatus?.mapToRoomEntity(),
            courseAssignments = courseAssignments?.mapToRoomEntity()
        )
    }
}
