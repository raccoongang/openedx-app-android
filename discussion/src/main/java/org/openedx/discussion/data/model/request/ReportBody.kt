package org.openedx.discussion.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportBody(
    @SerialName("abuse_flagged")
    val abuseFlagged: Boolean
)
