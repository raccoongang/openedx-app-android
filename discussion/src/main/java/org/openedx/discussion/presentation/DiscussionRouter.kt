package org.openedx.discussion.presentation

import org.openedx.core.FragmentViewType
import org.openedx.discussion.domain.model.DiscussionComment

interface DiscussionRouter {

    fun navigateToDiscussionThread(
        fm: Any?,
        action: String,
        courseId: String,
        topicId: String,
        title: String,
        viewType: FragmentViewType
    )

    fun navigateToDiscussionComments(
        fm: Any?,
        thread: org.openedx.discussion.domain.model.Thread
    )

    fun navigateToDiscussionResponses(
        fm: Any?,
        comment: DiscussionComment,
        isClosed: Boolean
    )

    fun navigateToAddThread(
        fm: Any?,
        topicId: String,
        courseId: String
    )

    fun navigateToSearchThread(
        fm: Any?,
        courseId: String
    )

    fun navigateToAnothersProfile(
        fm: Any?,
        username: String
    )
}
