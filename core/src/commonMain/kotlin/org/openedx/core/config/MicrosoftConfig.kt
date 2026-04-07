package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MicrosoftConfig(
    @SerialName("ENABLED")
    private val enabled: Boolean = false,
    @SerialName("CLIENT_ID")
    val clientId: String = "",
    @SerialName("PACKAGE_SIGNATURE")
    val packageSignature: String = "",
) {
    fun isEnabled() = enabled && clientId.isNotBlank() && packageSignature.isNotBlank()
}
