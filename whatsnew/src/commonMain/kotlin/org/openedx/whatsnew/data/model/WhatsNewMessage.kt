package org.openedx.whatsnew.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.openedx.core.Res as coreRes
import org.openedx.core.core_no_image_course
import org.openedx.whatsnew.Res
import org.openedx.whatsnew.screen_1
import org.openedx.whatsnew.screen_2
import org.openedx.whatsnew.screen_3

@Serializable
data class WhatsNewMessage(
    @SerialName("image")
    val image: String,
    @SerialName("title")
    val title: String,
    @SerialName("message")
    val message: String
) {
    fun mapToDomain() = org.openedx.whatsnew.domain.model.WhatsNewMessage(
        image = getDrawableFromString(image),
        title = title,
        message = message
    )

    private fun getDrawableFromString(imageName: String): DrawableResource {
        return when (imageName) {
            "screen_1" -> Res.drawable.screen_1
            "screen_2" -> Res.drawable.screen_2
            "screen_3" -> Res.drawable.screen_3
            else -> coreRes.drawable.core_no_image_course
        }
    }
}
