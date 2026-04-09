package org.openedx.core.domain.model

import kotlinx.datetime.Instant

data class EnrolledCourseData(
    val id: String,
    val name: String,
    val number: String,
    val org: String,
    val start: Instant?,
    val startDisplay: String,
    val startType: String,
    val end: Instant?,
    val dynamicUpgradeDeadline: String,
    val subscriptionId: String,
    val coursewareAccess: CoursewareAccess?,
    val media: Media?,
    val courseImage: String,
    val courseAbout: String,
    val courseSharingUtmParameters: CourseSharingUtmParameters,
    val courseUpdates: String,
    val courseHandouts: String,
    val discussionUrl: String,
    val videoOutline: String,
    val isSelfPaced: Boolean
)
