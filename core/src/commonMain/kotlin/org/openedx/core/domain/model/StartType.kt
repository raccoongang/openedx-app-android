package org.openedx.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class StartType(val type: String) {
    /**
     * Course's start date is provided as an unformatted string
     */
    @SerialName("string")
    STRING("string"),

    /**
     * Course's start date is provided as a date-formatted string
     */
    @SerialName("timestamp")
    TIMESTAMP("timestamp"),

    /**
     * Course's start date is unset
     */
    @SerialName("empty")
    EMPTY("empty")
}
