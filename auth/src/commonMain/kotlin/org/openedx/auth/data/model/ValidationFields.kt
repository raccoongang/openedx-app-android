package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidationFields(
    @SerialName("validation_decisions")
    val validationResult: Map<String, String>
) {
    fun hasValidationError() = validationResult.values.any { it != "" }
}
