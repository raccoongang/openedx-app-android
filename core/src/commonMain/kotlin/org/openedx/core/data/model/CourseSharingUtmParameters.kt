package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseSharingUtmParameters

@Serializable
data class CourseSharingUtmParameters(
    @SerialName("facebook")
    val facebook: String? = null,
    @SerialName("twitter")
    val twitter: String? = null
) {
    fun mapToDomain(): CourseSharingUtmParameters {
        return CourseSharingUtmParameters(
            facebook = facebook ?: "",
            twitter = twitter ?: ""
        )
    }
}
