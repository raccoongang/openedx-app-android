package org.openedx.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class FirebaseConfig(
    @SerialName("ENABLED")
    val enabled: Boolean = false,

    @SerialName("CLOUD_MESSAGING_ENABLED")
    val isCloudMessagingEnabled: Boolean = false,

    @SerialName("PROJECT_NUMBER")
    val projectNumber: String = "",

    @SerialName("PROJECT_ID")
    val projectId: String = "",

    @SerialName("APPLICATION_ID")
    val applicationId: String = "",

    @SerialName("API_KEY")
    val apiKey: String = "",
)
