package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseAssignments

@Serializable
data class CourseAssignments(
    @SerialName("future_assignments")
    val futureAssignments: List<CourseDateBlock>? = null,
    @SerialName("past_assignments")
    val pastAssignments: List<CourseDateBlock>? = null,
) {
    fun mapToDomain() = CourseAssignments(
        futureAssignments = futureAssignments?.mapNotNull {
            it.mapToDomain()
        },
        pastAssignments = pastAssignments?.mapNotNull {
            it.mapToDomain()
        }
    )
}
