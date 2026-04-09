package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.utils.TimeUtils
import org.openedx.core.domain.model.CourseInfoOverview as DomainCourseInfoOverview

@Serializable
data class CourseInfoOverview(
    @SerialName("name")
    val name: String,
    @SerialName("number")
    val number: String,
    @SerialName("org")
    val org: String,
    @SerialName("start")
    val start: String?,
    @SerialName("start_display")
    val startDisplay: String,
    @SerialName("start_type")
    val startType: String,
    @SerialName("end")
    val end: String?,
    @SerialName("is_self_paced")
    val isSelfPaced: Boolean,
    @SerialName("media")
    var media: Media?,
    @SerialName("course_sharing_utm_parameters")
    val courseSharingUtmParameters: CourseSharingUtmParameters,
    @SerialName("course_about")
    val courseAbout: String,
) {
    fun mapToDomain() = DomainCourseInfoOverview(
        name = name,
        number = number,
        org = org,
        start = TimeUtils.iso8601ToDate(start ?: ""),
        startDisplay = startDisplay,
        startType = startType,
        end = TimeUtils.iso8601ToDate(end ?: ""),
        isSelfPaced = isSelfPaced,
        media = media?.mapToDomain(),
        courseSharingUtmParameters = courseSharingUtmParameters.mapToDomain(),
        courseAbout = courseAbout,
    )
}
