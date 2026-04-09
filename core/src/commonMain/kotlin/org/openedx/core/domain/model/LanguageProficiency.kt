package org.openedx.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageProficiency(
    @SerialName("code")
    val code: String
)
