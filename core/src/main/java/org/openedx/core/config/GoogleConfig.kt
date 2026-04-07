package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class GoogleConfig(
    @SerialName("ENABLED")
    private val enabled: Boolean = false,
    @SerialName("CLIENT_ID")
    val clientId: String = "",
) {
    fun isEnabled() = enabled && clientId.isNotBlank()
}
