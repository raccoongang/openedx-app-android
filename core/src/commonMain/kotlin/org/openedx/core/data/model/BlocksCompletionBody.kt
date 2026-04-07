package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocksCompletionBody(
    @SerialName("username")
    val username: String,
    @SerialName("course_key")
    val courseId: String,
    @SerialName("blocks")
    val blocks: Map<String, String>
)
