package org.openedx.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class CourseInfoOverview(
    val name: String,
    val number: String,
    val org: String,
    val start: Instant?,
    val startDisplay: String?,
    val startType: String,
    val end: Instant?,
    val isSelfPaced: Boolean,
    var media: Media?,
    val courseSharingUtmParameters: CourseSharingUtmParameters,
    val courseAbout: String,
) {
    val isStarted: Boolean
        get() = start?.let { it < Clock.System.now() } ?: false
}
