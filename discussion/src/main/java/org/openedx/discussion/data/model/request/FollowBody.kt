package org.openedx.discussion.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class FollowBody(
    @SerialName("following")
    val following: Boolean
)
