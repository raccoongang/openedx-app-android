package org.openedx.whatsnew.data.model

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WhatsNewItem(
    @SerialName("version")
    val version: String,
    @SerialName("messages")
    val messages: List<WhatsNewMessage>
) {
    fun mapToDomain(context: Context) = org.openedx.whatsnew.domain.model.WhatsNewItem(
        version = version,
        messages = messages.map { it.mapToDomain(context) }
    )
}
