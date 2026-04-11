package org.openedx.discussion.presentation.threads

import org.jetbrains.compose.resources.StringResource
import org.openedx.discussion.Res
import org.openedx.discussion.discussion_most_activity
import org.openedx.discussion.discussion_most_votes
import org.openedx.discussion.discussion_recent_activity

enum class SortType(
    val textRes: StringResource,
    val queryParam: String
) {
    LAST_ACTIVITY_AT(textRes = Res.string.discussion_recent_activity, queryParam = "last_activity_at"),
    COMMENT_COUNT(textRes = Res.string.discussion_most_activity, queryParam = "comment_count"),
    VOTE_COUNT(textRes = Res.string.discussion_most_votes, queryParam = "vote_count");

    companion object {
        const val type = "sort_type"
    }
}
