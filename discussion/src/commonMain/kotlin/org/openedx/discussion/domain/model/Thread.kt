package org.openedx.discussion.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

import org.openedx.core.domain.model.ProfileImage

@Serializable
data class Thread(
    val id: String,
    val author: String,
    val authorLabel: String,
    val createdAt: String,
    val updatedAt: String,
    val rawBody: String,
    val renderedBody: String,
    val abuseFlagged: Boolean,
    val voted: Boolean,
    val voteCount: Int,
    val editableFields: List<String>,
    val canDelete: Boolean,
    val courseId: String,
    val topicId: String,
    val groupId: String,
    val groupName: String,
    val type: DiscussionType,
    val previewBody: String,
    val abuseFlaggedCount: String,
    val title: String,
    val pinned: Boolean,
    val closed: Boolean,
    val following: Boolean,
    val commentCount: Int,
    val unreadCommentCount: Int,
    val read: Boolean,
    val hasEndorsed: Boolean,
    val users: Map<String, DiscussionProfile>?,
    val responseCount: Int,
    val anonymous: Boolean,
    val anonymousToPeers: Boolean
)

@Serializable
data class DiscussionProfile(
    val image: ProfileImage?
)

enum class DiscussionType(
    val value: String,
) {
    QUESTION("question"),
    DISCUSSION("discussion")
}
