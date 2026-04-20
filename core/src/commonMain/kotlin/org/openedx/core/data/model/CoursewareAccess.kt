package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CoursewareAccess

@Serializable
data class CoursewareAccess(
    @SerialName("has_access")
    val hasAccess: Boolean? = null,
    @SerialName("error_code")
    val errorCode: String? = null,
    @SerialName("developer_message")
    val developerMessage: String? = null,
    @SerialName("user_message")
    val userMessage: String? = null,
    @SerialName("additional_context_user_message")
    val additionalContextUserMessage: String? = null,
    @SerialName("user_fragment")
    val userFragment: String? = null
) {

    fun mapToDomain(): CoursewareAccess {
        return CoursewareAccess(
            hasAccess = hasAccess ?: false,
            errorCode = errorCode ?: "",
            developerMessage = developerMessage ?: "",
            userMessage = userMessage ?: "",
            additionalContextUserMessage = additionalContextUserMessage ?: "",
            userFragment = userFragment ?: ""
        )
    }
}
