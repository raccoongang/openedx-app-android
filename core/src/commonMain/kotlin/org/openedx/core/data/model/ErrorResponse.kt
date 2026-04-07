package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class ErrorResponse(
    @JsonNames("error", "error_code")
    val error: String? = null,
    @JsonNames("error_description", "value", "developer_message")
    val errorDescription: String? = null,
)
