package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.auth.domain.model.AuthResponse

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    var accessToken: String? = null,
    @SerialName("token_type")
    var tokenType: String? = null,
    @SerialName("expires_in")
    var expiresIn: Long? = null,
    @SerialName("scope")
    var scope: String? = null,
    @SerialName("error")
    var error: String? = null,
    @SerialName("refresh_token")
    var refreshToken: String? = null,
) {
    fun mapToDomain(): AuthResponse {
        return AuthResponse(
            accessToken = accessToken,
            tokenType = tokenType,
            expiresIn = expiresIn?.times(1000),
            scope = scope,
            error = error,
            refreshToken = refreshToken,
        )
    }
}
