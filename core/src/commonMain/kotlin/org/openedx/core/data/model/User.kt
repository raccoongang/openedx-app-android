package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.domain.model.User

@Serializable
data class User(
    @SerialName("id")
    val id: Long,
    @SerialName("username")
    val username: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("name")
    val name: String? = null
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
