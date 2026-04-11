package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.EnrolledCourseData
import org.openedx.core.utils.InstantUtils

@Serializable
data class EnrolledCourseData(
    @SerialName("id")
    var id: String?,
    @SerialName("name")
    var name: String?,
    @SerialName("number")
    var number: String?,
    @SerialName("org")
    var org: String?,
    @SerialName("start")
    var start: String?,
    @SerialName("start_display")
    var startDisplay: String?,
    @SerialName("start_type")
    var startType: String?,
    @SerialName("end")
    var end: String?,
    @SerialName("dynamic_upgrade_deadline")
    var dynamicUpgradeDeadline: String?,
    @SerialName("subscription_id")
    var subscriptionId: String?,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess?,
    @SerialName("media")
    var media: Media?,
    @SerialName("course_image")
    var courseImage: String?,
    @SerialName("course_about")
    var courseAbout: String?,
    @SerialName("course_sharing_utm_parameters")
    var courseSharingUtmParameters: CourseSharingUtmParameters?,
    @SerialName("course_updates")
    var courseUpdates: String?,
    @SerialName("course_handouts")
    var courseHandouts: String?,
    @SerialName("discussion_url")
    var discussionUrl: String?,
    @SerialName("video_outline")
    var videoOutline: String?,
    @SerialName("is_self_paced")
    var isSelfPaced: Boolean?
) {

    fun mapToDomain(): EnrolledCourseData {
        return EnrolledCourseData(
            id = id.orEmpty(),
            name = name.orEmpty(),
            number = number.orEmpty(),
            org = org.orEmpty(),
            start = parseDate(start),
            startDisplay = startDisplay.orEmpty(),
            startType = startType.orEmpty(),
            end = parseDate(end),
            dynamicUpgradeDeadline = dynamicUpgradeDeadline.orEmpty(),
            subscriptionId = subscriptionId.orEmpty(),
            coursewareAccess = coursewareAccess?.mapToDomain(),
            media = media?.mapToDomain(),
            courseImage = courseImage.orEmpty(),
            courseAbout = courseAbout.orEmpty(),
            courseSharingUtmParameters = courseSharingUtmParameters?.mapToDomain()!!,
            courseUpdates = courseUpdates.orEmpty(),
            courseHandouts = courseHandouts.orEmpty(),
            discussionUrl = discussionUrl.orEmpty(),
            videoOutline = videoOutline.orEmpty(),
            isSelfPaced = isSelfPaced ?: false
        )
    }

    private fun parseDate(date: String?) = InstantUtils.iso8601ToInstant(date.orEmpty())
}
