package org.openedx.discovery.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.Media
import org.openedx.core.utils.InstantUtils
import org.openedx.discovery.domain.model.Course

@Serializable
data class CourseDetails(
    @SerialName("blocks_url")
    val blocksUrl: String? = null,
    @SerialName("course_id")
    val courseId: String? = null,
    @SerialName("effort")
    val effort: String? = null,
    @SerialName("end")
    val end: String? = null,
    @SerialName("enrollment_end")
    val enrollmentEnd: String? = null,
    @SerialName("enrollment_start")
    val enrollmentStart: String? = null,
    @SerialName("hidden")
    val hidden: Boolean? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("invitation_only")
    val invitationOnly: Boolean? = null,
    @SerialName("media")
    val media: Media? = null,
    @SerialName("mobile_available")
    val mobileAvailable: Boolean? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("number")
    val number: String? = null,
    @SerialName("org")
    val organization: String? = null,
    @SerialName("pacing")
    val pacing: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    @SerialName("start")
    val start: String? = null,
    @SerialName("start_display")
    val startDisplay: String? = null,
    @SerialName("start_type")
    val startType: String? = null,
    @SerialName("overview")
    val overview: String? = null,
    @SerialName("is_enrolled")
    val isEnrolled: Boolean? = null,
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

    private fun parseEnrollmentStartDate() = InstantUtils.iso8601ToInstant(enrollmentStart.orEmpty())

    private fun parseEnrollmentEndDate() = InstantUtils.iso8601ToInstant(enrollmentEnd.orEmpty())

    private fun mapMediaToDomain() = media?.mapToDomain() ?: org.openedx.core.domain.model.Media()
}
