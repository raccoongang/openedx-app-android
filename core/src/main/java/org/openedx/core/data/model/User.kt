package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.User

data class User(
    @SerialName("id")
    val id: Long,
    @SerialName("username")
    val username: String?,
    @SerialName("email")
    val email: String?,
    @SerialName("name")
    val name: String?
) {
    fun mapToDomain(): User {
        return User(
            id,
            username,
            email,
            name ?: ""
        )
    }
}
