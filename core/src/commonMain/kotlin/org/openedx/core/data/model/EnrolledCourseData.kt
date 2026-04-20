package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.EnrolledCourseData
import org.openedx.core.utils.InstantUtils

@Serializable
data class EnrolledCourseData(
    @SerialName("id")
    var id: String? = null,
    @SerialName("name")
    var name: String? = null,
    @SerialName("number")
    var number: String? = null,
    @SerialName("org")
    var org: String? = null,
    @SerialName("start")
    var start: String? = null,
    @SerialName("start_display")
    var startDisplay: String? = null,
    @SerialName("start_type")
    var startType: String? = null,
    @SerialName("end")
    var end: String? = null,
    @SerialName("dynamic_upgrade_deadline")
    var dynamicUpgradeDeadline: String? = null,
    @SerialName("subscription_id")
    var subscriptionId: String? = null,
    @SerialName("courseware_access")
    var coursewareAccess: CoursewareAccess? = null,
    @SerialName("media")
    var media: Media? = null,
    @SerialName("course_image")
    var courseImage: String? = null,
    @SerialName("course_about")
    var courseAbout: String? = null,
    @SerialName("course_sharing_utm_parameters")
    var courseSharingUtmParameters: CourseSharingUtmParameters? = null,
    @SerialName("course_updates")
    var courseUpdates: String? = null,
    @SerialName("course_handouts")
    var courseHandouts: String? = null,
    @SerialName("discussion_url")
    var discussionUrl: String? = null,
    @SerialName("video_outline")
    var videoOutline: String? = null,
    @SerialName("is_self_paced")
    var isSelfPaced: Boolean? = null
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
