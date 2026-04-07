package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.ResetCourseDates

@Serializable
data class ResetCourseDates(
    @SerialName("message")
    val message: String = "",
    @SerialName("body")
    val body: String = "",
    @SerialName("header")
    val header: String = "",
    @SerialName("link")
    val link: String = "",
    @SerialName("link_text")
    val linkText: String = "",
) {
    fun mapToDomain(): ResetCourseDates {
        return ResetCourseDates(
            message = message,
            body = body,
            header = header,
            link = link,
            linkText = linkText,
        )
    }
}
