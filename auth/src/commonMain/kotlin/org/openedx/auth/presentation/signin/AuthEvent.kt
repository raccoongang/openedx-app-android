package org.openedx.auth.presentation.signin

import org.openedx.auth.data.model.AuthType

sealed interface AuthEvent {
    data class SignIn(val login: String, val password: String) : AuthEvent
    data class SocialSignIn(val authType: AuthType) : AuthEvent
    data class OpenLink(val links: Map<String, String>, val link: String) : AuthEvent
    object SignInBrowser : AuthEvent
    object RegisterClick : AuthEvent
    object ForgotPasswordClick : AuthEvent
    object BackClick : AuthEvent
}
