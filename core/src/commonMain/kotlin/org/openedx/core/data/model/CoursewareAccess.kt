package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.CoursewareAccess

@Serializable
data class CoursewareAccess(
    @SerialName("has_access")
    val hasAccess: Boolean?,
    @SerialName("error_code")
    val errorCode: String?,
    @SerialName("developer_message")
    val developerMessage: String?,
    @SerialName("user_message")
    val userMessage: String?,
    @SerialName("additional_context_user_message")
    val additionalContextUserMessage: String?,
    @SerialName("user_fragment")
    val userFragment: String?
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
