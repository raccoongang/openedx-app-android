package org.openedx.discussion.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentBody(
    @SerialName("thread_id")
    val threadId: String,
    @SerialName("raw_body")
    val rawBody: String,
    @SerialName("parent_id")
    val parentId: String?
)
