package org.openedx.discussion.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThreadBody(
    @SerialName("type")
    val type: String,
    @SerialName("topic_id")
    val topicId: String,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("title")
    val title: String,
    @SerialName("raw_body")
    val rawBody: String,
    @SerialName("following")
    val following: Boolean = true
)
