package org.openedx.whatsnew.data.model

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class WhatsNewMessage(
    @SerialName("image")
    val image: String,
    @SerialName("title")
    val title: String,
    @SerialName("message")
    val message: String
) {
    fun mapToDomain(context: Context) = org.openedx.whatsnew.domain.model.WhatsNewMessage(
        image = getDrawableIntFromString(context, image),
        title = title,
        message = message
    )

    private fun getDrawableIntFromString(context: Context, imageName: String): Int {
        val imageInt = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (imageInt == 0) {
            org.openedx.core.R.drawable.core_no_image_course
        } else {
            imageInt
        }
    }
}
