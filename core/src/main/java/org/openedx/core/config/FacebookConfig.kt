package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class FacebookConfig(
    @SerialName("ENABLED")
    private val enabled: Boolean = false,
    @SerialName("FACEBOOK_APP_ID")
    val appId: String = "",
    @SerialName("CLIENT_TOKEN")
    val clientToken: String = "",
) {
    fun isEnabled() = enabled && appId.isNotBlank() && clientToken.isNotBlank()
}
