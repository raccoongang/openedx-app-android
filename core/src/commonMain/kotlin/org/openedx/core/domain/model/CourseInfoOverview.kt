package org.openedx.core.domain.model

import java.util.Date

data class CourseInfoOverview(
    val name: String,
    val number: String,
    val org: String,
    val start: Date?,
    val startDisplay: String?,
    val startType: String,
    val end: Date?,
    val isSelfPaced: Boolean,
    var media: Media?,
    val courseSharingUtmParameters: CourseSharingUtmParameters,
    val courseAbout: String,
) {
    val isStarted: Boolean
        get() = start?.before(Date()) ?: false
}
