package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class BranchConfig(
    @SerialName("ENABLED")
    val enabled: Boolean = false,

    @SerialName("KEY")
    val key: String = "",

    @SerialName("URI_SCHEME")
    val uriScheme: String = "",

    @SerialName("HOST")
    val host: String = "",

    @SerialName("ALTERNATE_HOST")
    val alternateHost: String = "",
)
