package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.utils.InstantUtils
import org.openedx.core.domain.model.CourseDate as DomainCourseDate
import org.openedx.core.domain.model.CourseDatesResponse as DomainCourseDatesResponse

@Serializable
data class CourseDate(
    @SerialName("course_id")
    val courseId: String,
    @SerialName("first_component_block_id")
    val firstComponentBlockId: String?,
    @SerialName("due_date")
    val dueDate: String?,
    @SerialName("assignment_title")
    val assignmentTitle: String?,
    @SerialName("learner_has_access")
    val learnerHasAccess: Boolean?,
    @SerialName("relative")
    val relative: Boolean?,
    @SerialName("course_name")
    val courseName: String?
) {
    fun mapToDomain(): DomainCourseDate? {
        val dueDate = InstantUtils.iso8601ToInstant(dueDate ?: "")
        return DomainCourseDate(
            courseId = courseId,
            firstComponentBlockId = firstComponentBlockId ?: "",
            dueDate = dueDate ?: return null,
            assignmentTitle = assignmentTitle ?: "",
            learnerHasAccess = learnerHasAccess ?: false,
            courseName = courseName ?: "",
            relative = relative ?: false
        )
    }
}

@Serializable
data class CourseDatesResponse(
    @SerialName("count")
    val count: Int,
    @SerialName("next")
    val next: String?,
    @SerialName("previous")
    val previous: String?,
    @SerialName("results")
    val results: List<CourseDate>
) {
    fun mapToDomain(): DomainCourseDatesResponse {
        return DomainCourseDatesResponse(
            count = count,
            next = next,
            previous = previous,
            results = results.mapNotNull { it.mapToDomain() }
        )
    }
}
