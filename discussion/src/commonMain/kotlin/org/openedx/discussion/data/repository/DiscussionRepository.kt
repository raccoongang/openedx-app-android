package org.openedx.discussion.data.repository

import org.openedx.discussion.domain.model.CommentsData
import org.openedx.discussion.domain.model.DiscussionComment
import org.openedx.discussion.domain.model.ThreadsData
import org.openedx.discussion.domain.model.Topic

interface DiscussionRepository {
    suspend fun getCourseTopics(courseId: String): List<Topic>
    fun getCachedTopics(courseId: String): List<Topic>
    suspend fun getCourseThreads(
        courseId: String,
        following: Boolean?,
        topicId: String?,
        orderBy: String,
        view: String?,
        page: Int,
    ): ThreadsData

    suspend fun getCourseThread(
        threadId: String,
        courseId: String,
        topicId: String,
    ): org.openedx.discussion.domain.model.Thread

    suspend fun searchThread(courseId: String, query: String, page: Int): ThreadsData
    suspend fun getThreadComments(threadId: String, page: Int): CommentsData
    suspend fun getResponse(responseId: String): DiscussionComment
    suspend fun getThreadQuestionComments(threadId: String, endorsed: Boolean, page: Int): CommentsData
    suspend fun setThreadRead(threadId: String): org.openedx.discussion.domain.model.Thread
    suspend fun setThreadVoted(threadId: String, isVoted: Boolean): org.openedx.discussion.domain.model.Thread
    suspend fun setThreadFlagged(threadId: String, abuseFlagged: Boolean): org.openedx.discussion.domain.model.Thread
    suspend fun setThreadFollowed(threadId: String, following: Boolean): org.openedx.discussion.domain.model.Thread
    suspend fun setCommentVoted(commentId: String, isVoted: Boolean): DiscussionComment
    suspend fun setCommentFlagged(commentId: String, abuseFlagged: Boolean): DiscussionComment
    suspend fun getCommentsResponses(commentId: String, page: Int): CommentsData
    suspend fun createComment(threadId: String, rawBody: String, parentId: String?): DiscussionComment
    suspend fun createThread(
        topicId: String,
        courseId: String,
        type: String,
        title: String,
        rawBody: String,
        follow: Boolean,
    ): org.openedx.discussion.domain.model.Thread

    suspend fun markBlocksCompletion(courseId: String, blocksId: List<String>)
}
