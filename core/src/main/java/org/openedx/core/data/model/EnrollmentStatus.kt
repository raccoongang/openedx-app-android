package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.EnrollmentStatus

@Serializable
data class EnrollmentStatus(
    @SerialName("course_id")
    val courseId: String?,
    @SerialName("course_name")
    val courseName: String?,
    @SerialName("recently_active")
    val recentlyActive: Boolean?
) {
    fun mapToDomain() = EnrollmentStatus(
        courseId = courseId ?: "",
        courseName = courseName ?: "",
        recentlyActive = recentlyActive ?: false
    )
}
