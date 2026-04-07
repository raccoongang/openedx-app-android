package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementModel(
    @SerialName("date")
    val date: String,
    @SerialName("content")
    val content: String
) {
    fun mapToDomain() = org.openedx.core.domain.model.AnnouncementModel(
        date,
        content
    )
}
