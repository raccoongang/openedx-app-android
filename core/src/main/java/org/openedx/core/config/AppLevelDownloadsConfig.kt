package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AppLevelDownloadsConfig(
    @SerialName("ENABLED")
    val isEnabled: Boolean = true,
)
