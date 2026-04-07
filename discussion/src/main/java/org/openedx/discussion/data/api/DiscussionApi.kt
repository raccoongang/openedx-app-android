package org.openedx.discussion.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.openedx.core.data.model.BlocksCompletionBody
import org.openedx.discussion.data.model.request.CommentBody
import org.openedx.discussion.data.model.request.FollowBody
import org.openedx.discussion.data.model.request.ReadBody
import org.openedx.discussion.data.model.request.ReportBody
import org.openedx.discussion.data.model.request.ThreadBody
import org.openedx.discussion.data.model.request.VoteBody
import org.openedx.discussion.data.model.response.CommentResult
import org.openedx.discussion.data.model.response.CommentsResponse
import org.openedx.discussion.data.model.response.ThreadsResponse
import org.openedx.discussion.data.model.response.ThreadsResponse.Thread
import org.openedx.discussion.data.model.response.TopicsResponse

class DiscussionApi(private val client: HttpClient) {

    suspend fun getCourseTopics(courseId: String): TopicsResponse {
        return client.get("/api/discussion/v1/course_topics/$courseId").body()
    }

    suspend fun getCourseThreads(
        courseId: String,
        following: Boolean?,
        topicId: String?,
        orderBy: String,
        view: String?,
        page: Int = 1,
        requestedFields: List<String> = listOf("profile_image"),
    ): ThreadsResponse {
        return client.get("/api/discussion/v1/threads/") {
            parameter("course_id", courseId)
            following?.let { parameter("following", it) }
            topicId?.let { parameter("topic_id", it) }
            parameter("order_by", orderBy)
            view?.let { parameter("view", it) }
            parameter("page", page)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    suspend fun getCourseThread(
        threadId: String,
        courseId: String,
        topicId: String,
        requestedFields: List<String> = listOf("profile_image"),
    ): Thread {
        return client.get("/api/discussion/v1/threads/$threadId") {
            parameter("course_id", courseId)
            parameter("topic_id", topicId)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    suspend fun searchThreads(
        courseId: String,
        query: String,
        page: Int = 1,
        requestedFields: List<String> = listOf("profile_image"),
    ): ThreadsResponse {
        return client.get("/api/discussion/v1/threads/") {
            parameter("course_id", courseId)
            parameter("text_search", query)
            parameter("page", page)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    suspend fun getThreadComments(
        threadId: String,
        page: Int,
        requestedFields: List<String> = listOf("profile_image"),
    ): CommentsResponse {
        return client.get("/api/discussion/v1/comments/") {
            parameter("thread_id", threadId)
            parameter("page", page)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    suspend fun getResponse(responseId: String): CommentResult {
        return client.patch("/api/discussion/v1/comments/$responseId/") {
            contentType(ContentType("application", "merge-patch+json"))
        }.body()
    }

    suspend fun getThreadQuestionComments(
        threadId: String,
        page: Int,
        endorsed: Boolean,
        requestedFields: List<String> = listOf("profile_image"),
    ): CommentsResponse {
        return client.get("/api/discussion/v1/comments/") {
            parameter("thread_id", threadId)
            parameter("page", page)
            parameter("endorsed", endorsed)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    private fun mergePatchJson() = ContentType("application", "merge-patch+json")

    suspend fun setThreadRead(threadId: String, body: ReadBody): Thread {
        return client.patch("/api/discussion/v1/threads/$threadId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(body)
        }.body()
    }

    suspend fun setThreadVoted(threadId: String, body: VoteBody): Thread {
        return client.patch("/api/discussion/v1/threads/$threadId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(body)
        }.body()
    }

    suspend fun setThreadFlagged(threadId: String, reportBody: ReportBody): Thread {
        return client.patch("/api/discussion/v1/threads/$threadId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(reportBody)
        }.body()
    }

    suspend fun setThreadFollowed(threadId: String, followBody: FollowBody): Thread {
        return client.patch("/api/discussion/v1/threads/$threadId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(followBody)
        }.body()
    }

    suspend fun setCommentVoted(commentId: String, voteBody: VoteBody): CommentResult {
        return client.patch("/api/discussion/v1/comments/$commentId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(voteBody)
        }.body()
    }

    suspend fun setCommentFlagged(commentId: String, reportBody: ReportBody): CommentResult {
        return client.patch("/api/discussion/v1/comments/$commentId/") {
            header("Cache-Control", "no-cache")
            contentType(mergePatchJson())
            setBody(reportBody)
        }.body()
    }

    suspend fun getCommentsResponses(
        commentId: String,
        page: Int,
        requestedFields: List<String> = listOf("profile_image"),
    ): CommentsResponse {
        return client.get("/api/discussion/v1/comments/$commentId/") {
            parameter("page", page)
            requestedFields.forEach { parameter("requested_fields", it) }
        }.body()
    }

    suspend fun createComment(commentBody: CommentBody): CommentResult {
        return client.post("/api/discussion/v1/comments/") {
            contentType(ContentType.Application.Json)
            setBody(commentBody)
        }.body()
    }

    suspend fun createThread(threadBody: ThreadBody): Thread {
        return client.post("/api/discussion/v1/threads/") {
            contentType(ContentType.Application.Json)
            setBody(threadBody)
        }.body()
    }

    suspend fun markBlocksCompletion(blocksCompletionBody: BlocksCompletionBody) {
        client.post("/api/completion/v1/completion-batch") {
            contentType(ContentType.Application.Json)
            setBody(blocksCompletionBody)
        }
    }
}
