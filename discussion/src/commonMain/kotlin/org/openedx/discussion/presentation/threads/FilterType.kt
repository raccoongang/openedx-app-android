package org.openedx.discussion.presentation.threads

import org.jetbrains.compose.resources.StringResource
import org.openedx.discussion.Res
import org.openedx.discussion.discussion_all_posts
import org.openedx.discussion.discussion_unanswered
import org.openedx.discussion.discussion_unread

enum class FilterType(
    val textRes: StringResource,
    val value: String
) {
    ALL_POSTS(textRes = Res.string.discussion_all_posts, value = "all_posts"),
    UNREAD(textRes = Res.string.discussion_unread, value = "unread"),
    UNANSWERED(textRes = Res.string.discussion_unanswered, value = "unanswered");

    companion object {
        const val type = "filter_type"
    }
}
