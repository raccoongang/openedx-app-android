package org.openedx.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileImage(
    val imageUrlFull: String,
    val imageUrlLarge: String,
    val imageUrlMedium: String,
    val imageUrlSmall: String,
    val hasImage: Boolean
)
