package org.openedx.discussion.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteBody(
    @SerialName("voted")
    val voted: Boolean
)
