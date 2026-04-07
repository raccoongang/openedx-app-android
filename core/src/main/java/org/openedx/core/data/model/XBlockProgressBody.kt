package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class XBlockProgressBody(
    @SerialName("body")
    val body: String
)
