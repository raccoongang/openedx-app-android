package org.openedx.auth.presentation.sso

import android.app.Activity
import org.openedx.auth.data.model.AuthType
import org.openedx.auth.domain.model.SocialAuthResponse

class OAuthHelper(
    private val facebookAuthHelper: FacebookAuthHelper,
    private val googleAuthHelper: GoogleAuthHelper,
    private val microsoftAuthHelper: MicrosoftAuthHelper,
) {
    /**
     * SDK integration guides:
     * https://developer.android.com/training/sign-in/credential-manager
     * https://developers.facebook.com/docs/facebook-login/android/
     * https://github.com/AzureAD/microsoft-authentication-library-for-android
     */
    internal suspend fun socialAuth(activity: Activity, authType: AuthType): SocialAuthResponse? {
        return when (authType) {
            AuthType.PASSWORD -> null
            AuthType.GOOGLE -> googleAuthHelper.socialAuth(activity)
            AuthType.FACEBOOK -> facebookAuthHelper.socialAuth(activity)
            AuthType.MICROSOFT -> microsoftAuthHelper.socialAuth(activity)
            AuthType.BROWSER -> null
        }
    }

    fun clear() {
        facebookAuthHelper.clear()
    }
}
