package org.openedx.auth.presentation.sso

import android.app.Activity
import org.openedx.auth.data.model.AuthType
import org.openedx.auth.domain.model.SocialAuthResponse

class SocialAuthProviderImpl(
    private val oAuthHelper: OAuthHelper,
    private val browserAuthHelper: BrowserAuthHelper,
) : SocialAuthProvider {

    override suspend fun socialAuth(activityContext: Any, authType: AuthType): SocialAuthResponse? {
        return oAuthHelper.socialAuth(activityContext as Activity, authType)
    }

    override suspend fun signInWithBrowser(activityContext: Any) {
        browserAuthHelper.signIn(activityContext as Activity)
    }

    override fun clearAuth() {
        oAuthHelper.clear()
    }
}
