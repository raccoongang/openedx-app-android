package org.openedx.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.auth.domain.model.AuthResponse

@Serializable
data class AuthResponse(
    @SerialName("access_token")
    var accessToken: String?,
    @SerialName("token_type")
    var tokenType: String?,
    @SerialName("expires_in")
    var expiresIn: Long?,
    @SerialName("scope")
    var scope: String?,
    @SerialName("error")
    var error: String?,
    @SerialName("refresh_token")
    var refreshToken: String?,
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
