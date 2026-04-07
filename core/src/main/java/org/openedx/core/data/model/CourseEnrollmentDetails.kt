package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseEnrollmentDetails as DomainCourseEnrollmentDetails

@Serializable
data class CourseEnrollmentDetails(
    @SerialName("id")
    val id: String,
    @SerialName("course_updates")
    val courseUpdates: String?,
    @SerialName("course_handouts")
    val courseHandouts: String?,
    @SerialName("discussion_url")
    val discussionUrl: String?,
    @SerialName("course_access_details")
    val courseAccessDetails: CourseAccessDetails,
    @SerialName("certificate")
    val certificate: Certificate?,
    @SerialName("enrollment_details")
    val enrollmentDetails: EnrollmentDetails,
    @SerialName("course_info_overview")
    val courseInfoOverview: CourseInfoOverview,
) {
    fun mapToDomain(): DomainCourseEnrollmentDetails {
        return DomainCourseEnrollmentDetails(
            id = id,
            courseUpdates = courseUpdates ?: "",
            courseHandouts = courseHandouts ?: "",
            discussionUrl = discussionUrl ?: "",
            courseAccessDetails = courseAccessDetails.mapToDomain(),
            certificate = certificate?.mapToDomain(),
            enrollmentDetails = enrollmentDetails.mapToDomain(),
            courseInfoOverview = courseInfoOverview.mapToDomain(),
        )
    }
}
