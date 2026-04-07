package org.openedx.auth.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.parameters
import org.openedx.auth.data.model.AuthResponse
import org.openedx.auth.data.model.PasswordResetResponse
import org.openedx.auth.data.model.RegistrationFields
import org.openedx.auth.data.model.ValidationFields
import org.openedx.core.ApiConstants
import org.openedx.core.data.model.User

class AuthApi(private val client: HttpClient) {

    suspend fun exchangeAccessToken(
        accessToken: String,
        clientId: String,
        tokenType: String,
        isAsymmetricJwt: Boolean = true,
        authType: String,
    ): AuthResponse {
        val url = ApiConstants.URL_EXCHANGE_TOKEN.replace("{auth_type}", authType)
        return client.submitForm(url, parameters {
            append("access_token", accessToken)
            append("client_id", clientId)
            append("token_type", tokenType)
            append("asymmetric_jwt", isAsymmetricJwt.toString())
        }).body()
    }

    suspend fun getAccessToken(
        grantType: String,
        clientId: String,
        username: String,
        password: String,
        tokenType: String,
        isAsymmetricJwt: Boolean = true,
    ): AuthResponse {
        return client.submitForm(ApiConstants.URL_ACCESS_TOKEN, parameters {
            append("grant_type", grantType)
            append("client_id", clientId)
            append("username", username)
            append("password", password)
            append("token_type", tokenType)
            append("asymmetric_jwt", isAsymmetricJwt.toString())
        }).body()
    }

    suspend fun getAccessTokenFromCode(
        grantType: String,
        clientId: String,
        code: String,
        redirectUri: String,
        tokenType: String,
        isAsymmetricJwt: Boolean = true,
    ): AuthResponse {
        return client.submitForm(ApiConstants.URL_ACCESS_TOKEN, parameters {
            append("grant_type", grantType)
            append("client_id", clientId)
            append("code", code)
            append("redirect_uri", redirectUri)
            append("token_type", tokenType)
            append("asymmetric_jwt", isAsymmetricJwt.toString())
        }).body()
    }

    /**
     * Synchronous token refresh - used by the authenticator interceptor.
     * Returns the raw HttpResponse for manual handling.
     */
    suspend fun refreshAccessToken(
        grantType: String,
        clientId: String,
        refreshToken: String,
        tokenType: String,
        isAsymmetricJwt: Boolean = true,
    ): AuthResponse {
        return client.submitForm(ApiConstants.URL_ACCESS_TOKEN, parameters {
            append("grant_type", grantType)
            append("client_id", clientId)
            append("refresh_token", refreshToken)
            append("token_type", tokenType)
            append("asymmetric_jwt", isAsymmetricJwt.toString())
        }).body()
    }

    suspend fun getRegistrationFields(): RegistrationFields {
        return client.get(ApiConstants.URL_REGISTRATION_FIELDS).body()
    }

    suspend fun registerUser(fields: Map<String, String>) {
        client.submitForm(ApiConstants.URL_REGISTER, parameters {
            fields.forEach { (key, value) -> append(key, value) }
        })
    }

    suspend fun validateRegistrationFields(fields: Map<String, String>): ValidationFields {
        return client.submitForm(ApiConstants.URL_VALIDATE_REGISTRATION_FIELDS, parameters {
            fields.forEach { (key, value) -> append(key, value) }
        }).body()
    }

    suspend fun getProfile(): User {
        return client.get(ApiConstants.GET_USER_PROFILE).body()
    }

    suspend fun passwordReset(email: String): PasswordResetResponse {
        return client.submitForm(ApiConstants.URL_PASSWORD_RESET, parameters {
            append("email", email)
        }).body()
    }
}
