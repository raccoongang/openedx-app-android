package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.ProfileImage

@Serializable
data class ProfileImage(
    @SerialName("image_url_full")
    val imageUrlFull: String?,
    @SerialName("image_url_large")
    val imageUrlLarge: String?,
    @SerialName("image_url_medium")
    val imageUrlMedium: String?,
    @SerialName("image_url_small")
    val imageUrlSmall: String?,
    @SerialName("has_image")
    val hasImage: Boolean?,
) {

    fun mapToDomain(): ProfileImage {
        return ProfileImage(
            imageUrlFull = imageUrlFull ?: "",
            imageUrlLarge = imageUrlLarge ?: "",
            imageUrlMedium = imageUrlMedium ?: "",
            imageUrlSmall = imageUrlSmall ?: "",
            hasImage = hasImage ?: false
        )
    }
}
