package org.openedx.discussion.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.openedx.core.data.model.Pagination
import org.openedx.core.data.model.ProfileImage
import org.openedx.discussion.domain.model.DiscussionType
import org.openedx.discussion.domain.model.ThreadsData

@Serializable
data class ThreadsResponse(
    @SerialName("results")
    val results: List<Thread>,
    @SerialName("text_search_rewrite")
    val textSearchRewrite: String? = null,
    @SerialName("pagination")
    val pagination: Pagination
) {
    @Serializable
    data class Thread(
        @SerialName("id")
        val id: String,
        @SerialName("author")
        val author: String? = null,
        @SerialName("author_label")
        val authorLabel: String? = null,
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
        @SerialName("anonymous")
        val anonymous: Boolean,
        @SerialName("anonymous_to_peers")
        val anonymousToPeers: Boolean,
        @SerialName("course_id")
        val courseId: String,
        @SerialName("topic_id")
        val topicId: String,
        @SerialName("group_id")
        val groupId: String? = null,
        @SerialName("group_name")
        val groupName: String? = null,
        @SerialName("type")
        val type: String,
        @SerialName("preview_body")
        val previewBody: String,
        @SerialName("abuse_flagged_count")
        @Contextual val abuseFlaggedCount: Any? = null,
        @SerialName("title")
        val title: String,
        @SerialName("pinned")
        val pinned: Boolean,
        @SerialName("closed")
        val closed: Boolean,
        @SerialName("following")
        val following: Boolean,
        @SerialName("comment_count")
        val commentCount: Int,
        @SerialName("unread_comment_count")
        val unreadCommentCount: Int,
        @SerialName("read")
        val read: Boolean,
        @SerialName("has_endorsed")
        val hasEndorsed: Boolean,
        @SerialName("response_count")
        val responseCount: Int,
        @SerialName("users")
        val users: Map<String, DiscussionProfile>? = null
    ) {
    @Serializable
        data class DiscussionProfile(
            @SerialName("profile")
            val profile: ProfileResponse
        ) {
            fun mapToDomain(): org.openedx.discussion.domain.model.DiscussionProfile {
                return org.openedx.discussion.domain.model.DiscussionProfile(
                    image = profile.image.mapToDomain()
                )
            }
        }

    @Serializable
        data class ProfileResponse(
            @SerialName("image")
            val image: ProfileImage
        )

        fun mapToDomain(): org.openedx.discussion.domain.model.Thread {
            return org.openedx.discussion.domain.model.Thread(
                id,
                author ?: "",
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
                courseId,
                topicId,
                groupId ?: "",
                groupName ?: "",
                serverTypeToLocalType(),
                previewBody,
                "",
                title,
                pinned,
                closed,
                following,
                commentCount,
                unreadCommentCount,
                read,
                hasEndorsed,
                users?.entries?.associate { it.key to it.value.mapToDomain() },
                responseCount,
                anonymous,
                anonymousToPeers
            )
        }

        private fun serverTypeToLocalType(): DiscussionType {
            val actualType = if (type.contains("-")) {
                type.replace("-", "_")
            } else {
                type
            }
            return try {
                DiscussionType.valueOf(actualType.uppercase())
            } catch (e: Exception) {
                e.printStackTrace()
                error("Unknown thread type")
            }
        }
    }

    fun mapToDomain(): ThreadsData {
        return ThreadsData(
            results.map { it.mapToDomain() },
            textSearchRewrite ?: "",
            pagination.mapToDomain()
        )
    }
}
