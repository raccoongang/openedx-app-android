package org.openedx.core

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class NoContentScreenType(
    val iconResId: DrawableResource,
    val messageRes: StringResource,
) {
    COURSE_OUTLINE(
        iconResId = Res.drawable.core_ic_no_content,
        messageRes = Res.string.core_no_course_content
    ),
    COURSE_VIDEOS(
        iconResId = Res.drawable.core_ic_no_videos,
        messageRes = Res.string.core_no_videos
    ),
    COURSE_DATES(
        iconResId = Res.drawable.core_ic_no_content,
        messageRes = Res.string.core_no_dates
    ),
    COURSE_ASSIGNMENT(
        iconResId = Res.drawable.core_ic_no_content,
        messageRes = Res.string.core_no_assignments
    ),
    COURSE_DISCUSSIONS(
        iconResId = Res.drawable.core_ic_no_content,
        messageRes = Res.string.core_no_discussion
    ),
    COURSE_HANDOUTS(
        iconResId = Res.drawable.core_ic_no_handouts,
        messageRes = Res.string.core_no_handouts
    ),
    COURSE_ANNOUNCEMENTS(
        iconResId = Res.drawable.core_ic_no_announcements,
        messageRes = Res.string.core_no_announcements
    ),
    COURSE_PROGRESS(
        iconResId = Res.drawable.core_ic_no_content,
        messageRes = Res.string.core_no_progress
    ),
}
