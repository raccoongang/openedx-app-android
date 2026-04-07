package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class EnrollBody(
    @SerialName("course_details")
    val courseDetails: CourseDetails
) {
    data class CourseDetails(
        @SerialName("course_id")
        val courseId: String,
        @SerialName("email_opt_in")
        val emailOptIn: String?,
    )
}
