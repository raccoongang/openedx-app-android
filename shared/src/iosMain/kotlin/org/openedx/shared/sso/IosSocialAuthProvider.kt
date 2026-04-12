package org.openedx.shared.sso

import org.openedx.auth.data.model.AuthType
import org.openedx.auth.domain.model.SocialAuthResponse
import org.openedx.auth.presentation.sso.SocialAuthProvider

/**
 * iOS stub for SocialAuthProvider.
 * TODO: Implement using ASWebAuthenticationSession, Sign in with Apple, etc.
 */
class IosSocialAuthProvider : SocialAuthProvider {
    override suspend fun socialAuth(activityContext: Any, authType: AuthType): SocialAuthResponse? {
        // No-op: iOS social auth not yet implemented
        return null
    }

    override suspend fun signInWithBrowser(activityContext: Any) {
        // No-op: iOS browser auth not yet implemented
    }

    override fun clearAuth() {
        // No-op
    }
}
