package org.openedx.auth.presentation.sso

import org.openedx.auth.data.model.AuthType
import org.openedx.auth.domain.model.SocialAuthResponse

interface SocialAuthProvider {
    suspend fun socialAuth(activityContext: Any, authType: AuthType): SocialAuthResponse?
    suspend fun signInWithBrowser(activityContext: Any)
    fun clearAuth()
}
