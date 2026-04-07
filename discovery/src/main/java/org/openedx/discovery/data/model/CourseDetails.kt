package org.openedx.discovery.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.Media
import org.openedx.core.utils.TimeUtils
import org.openedx.discovery.domain.model.Course

data class CourseDetails(
    @SerialName("blocks_url")
    val blocksUrl: String?,
    @SerialName("course_id")
    val courseId: String?,
    @SerialName("effort")
    val effort: String?,
    @SerialName("end")
    val end: String?,
    @SerialName("enrollment_end")
    val enrollmentEnd: String?,
    @SerialName("enrollment_start")
    val enrollmentStart: String?,
    @SerialName("hidden")
    val hidden: Boolean?,
    @SerialName("id")
    val id: String?,
    @SerialName("invitation_only")
    val invitationOnly: Boolean?,
    @SerialName("media")
    val media: Media?,
    @SerialName("mobile_available")
    val mobileAvailable: Boolean?,
    @SerialName("name")
    val name: String?,
    @SerialName("number")
    val number: String?,
    @SerialName("org")
    val organization: String?,
    @SerialName("pacing")
    val pacing: String?,
    @SerialName("short_description")
    val shortDescription: String?,
    @SerialName("start")
    val start: String?,
    @SerialName("start_display")
    val startDisplay: String?,
    @SerialName("start_type")
    val startType: String?,
    @SerialName("overview")
    val overview: String?,
    @SerialName("is_enrolled")
    val isEnrolled: Boolean?,
) {

    fun mapToDomain(): Course {
        return Course(
            id = id.orEmpty(),
            blocksUrl = blocksUrl.orEmpty(),
            courseId = courseId.orEmpty(),
            effort = effort.orEmpty(),
            enrollmentStart = parseEnrollmentStartDate(),
            enrollmentEnd = parseEnrollmentEndDate(),
            hidden = hidden ?: false,
            invitationOnly = invitationOnly ?: false,
            mobileAvailable = mobileAvailable ?: false,
            name = name.orEmpty(),
            number = number.orEmpty(),
            org = organization.orEmpty(),
            shortDescription = shortDescription.orEmpty(),
            start = start.orEmpty(),
            end = end.orEmpty(),
            startDisplay = startDisplay.orEmpty(),
            startType = startType.orEmpty(),
            pacing = pacing.orEmpty(),
            overview = overview.orEmpty(),
            isEnrolled = isEnrolled ?: false,
            media = mapMediaToDomain()
        )
    }

    private fun parseEnrollmentStartDate() = TimeUtils.iso8601ToDate(enrollmentStart.orEmpty())

    private fun parseEnrollmentEndDate() = TimeUtils.iso8601ToDate(enrollmentEnd.orEmpty())

    private fun mapMediaToDomain() = media?.mapToDomain() ?: org.openedx.core.domain.model.Media()
}
