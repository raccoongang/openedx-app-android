package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AppLevelDatesConfig(
    @SerialName("ENABLED")
    val isEnabled: Boolean = true,
)
