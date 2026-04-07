package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrazeConfig(
    @SerialName("ENABLED")
    val isEnabled: Boolean = false,

    @SerialName("PUSH_NOTIFICATIONS_ENABLED")
    val isPushNotificationsEnabled: Boolean = false
)
