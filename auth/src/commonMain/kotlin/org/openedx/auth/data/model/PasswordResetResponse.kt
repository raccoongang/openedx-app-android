package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetResponse(
    @SerialName("success")
    val success: Boolean
)
