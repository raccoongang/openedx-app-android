package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.ProfileImage

@Serializable
data class ProfileImage(
    @SerialName("image_url_full")
    val imageUrlFull: String? = null,
    @SerialName("image_url_large")
    val imageUrlLarge: String? = null,
    @SerialName("image_url_medium")
    val imageUrlMedium: String? = null,
    @SerialName("image_url_small")
    val imageUrlSmall: String? = null,
    @SerialName("has_image")
    val hasImage: Boolean? = null,
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
