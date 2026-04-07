package org.openedx.auth.domain.model

import org.openedx.core.utils.TimeUtils

data class AuthResponse(
    var accessToken: String?,
    var tokenType: String?,
    var expiresIn: Long?,
    var scope: String?,
    var error: String?,
    var refreshToken: String?,
) {
    fun getTokenExpiryTime(): Long {
        return (expiresIn ?: 0L) + TimeUtils.getCurrentTime()
    }
}
