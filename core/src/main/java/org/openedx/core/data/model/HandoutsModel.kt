package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.HandoutsModel

data class HandoutsModel(
    @SerialName("handouts_html")
    val handoutsHtml: String
) {
    fun mapToDomain() = HandoutsModel(handoutsHtml)
}
