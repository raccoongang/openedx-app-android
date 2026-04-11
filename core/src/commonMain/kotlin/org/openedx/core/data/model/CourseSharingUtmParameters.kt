package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CourseSharingUtmParameters

@Serializable
data class CourseSharingUtmParameters(
    @SerialName("facebook")
    val facebook: String?,
    @SerialName("twitter")
    val twitter: String?
) {
    fun mapToDomain(): CourseSharingUtmParameters {
        return CourseSharingUtmParameters(
            facebook = facebook ?: "",
            twitter = twitter ?: ""
        )
    }
}
