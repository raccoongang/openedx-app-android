package org.openedx.discovery.domain.model

import kotlinx.datetime.Instant
import org.openedx.core.domain.model.Media

data class Course(
    val id: String,
    val blocksUrl: String,
    val courseId: String,
    val effort: String,
    val enrollmentStart: Instant?,
    val enrollmentEnd: Instant?,
    val hidden: Boolean,
    val invitationOnly: Boolean,
    val media: Media,
    val mobileAvailable: Boolean,
    val name: String,
    val number: String,
    val org: String,
    val pacing: String,
    val shortDescription: String,
    val start: String,
    val end: String,
    val startDisplay: String,
    val startType: String,
    val overview: String,
    val isEnrolled: Boolean
)
