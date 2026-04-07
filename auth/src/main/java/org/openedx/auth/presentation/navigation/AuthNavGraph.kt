package org.openedx.auth.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for the auth module.
 */
object AuthRoutes {
    @Serializable
    data class SignIn(
        val courseId: String = "",
        val infoType: String = "",
        val authCode: String = "",
    )

    @Serializable
    data class SignUp(
        val courseId: String = "",
        val infoType: String = "",
    )

    @Serializable
    object RestorePassword

    @Serializable
    data class Logistration(val courseId: String = "")
}
