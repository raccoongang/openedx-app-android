package org.openedx.discussion.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.Pagination
import org.openedx.core.data.model.ProfileImage
import org.openedx.discussion.domain.model.CommentsData
import org.openedx.discussion.domain.model.DiscussionComment

data class CommentsResponse(
    @SerialName("results")
    val results: List<CommentResult>,
    @SerialName("pagination")
    val pagination: Pagination
) {
    fun mapToDomain(): CommentsData {
        return CommentsData(
            results.map { it.mapToDomain() },
            pagination.mapToDomain()
        )
    }
}

data class CommentResult(
    @SerialName("id")
    val id: String,
    @SerialName("author")
    val author: String,
    @SerialName("author_label")
    val authorLabel: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("raw_body")
    val rawBody: String,
    @SerialName("rendered_body")
    val renderedBody: String,
    @SerialName("abuse_flagged")
    val abuseFlagged: Boolean,
    @SerialName("voted")
    val voted: Boolean,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("editable_fields")
    val editableFields: List<String>,
    @SerialName("can_delete")
    val canDelete: Boolean,
    @SerialName("thread_id")
    val threadId: String,
    @SerialName("parent_id")
    val parentId: String?,
    @SerialName("endorsed")
    val endorsed: Boolean,
    @SerialName("endorsed_by")
    val endorsedBy: String?,
    @SerialName("endorsed_by_label")
    val endorsedByLabel: String?,
    @SerialName("endorsed_at")
    val endorsedAt: String?,
    @SerialName("child_count")
    val childCount: Int,
    @SerialName("children")
    val children: List<String>,
    @SerialName("abuse_flagged_any_user")
    val abuseFlaggedAnyUser: String?,
    @SerialName("profile_image")
    val profileImage: ProfileImage?,
    @SerialName("users")
    val users: Map<String, ThreadsResponse.Thread.DiscussionProfile>?
) {
    fun mapToDomain(): DiscussionComment {
        return DiscussionComment(
            id,
            author,
            authorLabel ?: "",
            createdAt,
            updatedAt,
            rawBody,
            renderedBody,
            abuseFlagged,
            voted,
            voteCount,
            editableFields,
            canDelete,
            threadId,
            parentId ?: "",
            endorsed,
            endorsedBy ?: "",
            endorsedByLabel ?: "",
            endorsedAt ?: "",
            childCount,
            children,
            profileImage?.mapToDomain(),
            users?.entries?.associate { it.key to it.value.mapToDomain() }
        )
    }
}
