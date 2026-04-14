package org.openedx.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.openedx.auth.data.model.AuthResponse
import org.openedx.core.ApiConstants
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.LogoutEvent

/**
 * Installs token refresh interceptor on the Ktor HttpClient.
 *
 * On 401 Unauthorized response:
 * 1. Refresh the access token using the stored refresh token
 * 2. Save new tokens to CorePreferences
 * 3. Retry the original request with the new token
 * 4. If refresh fails → send LogoutEvent
 *
 * Mirrors Android's OauthRefreshTokenAuthenticator behavior.
 */
fun HttpClient.installTokenRefresh(
    config: Config,
    prefs: CorePreferences,
    appNotifier: AppNotifier,
) {
    val refreshMutex = Mutex()

    val refreshClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                coerceInputValues = true
                explicitNulls = false
            })
        }
        defaultRequest {
            url(config.getApiHostURL())
        }
    }

    plugin(HttpSend).intercept { request ->
        val originalCall = execute(request)

        if (originalCall.response.status != HttpStatusCode.Unauthorized) {
            return@intercept originalCall
        }

        val refreshToken = prefs.refreshToken
        if (refreshToken.isEmpty()) {
            kotlinx.coroutines.runBlocking { appNotifier.send(LogoutEvent(true)) }
            return@intercept originalCall
        }

        // Refresh token — only one refresh at a time
        val newToken = refreshMutex.withLock {
            try {
                val authResponse: AuthResponse = refreshClient.submitForm(
                    ApiConstants.URL_ACCESS_TOKEN,
                    parameters {
                        append("grant_type", ApiConstants.TOKEN_TYPE_REFRESH)
                        append("client_id", config.getOAuthClientId())
                        append("refresh_token", refreshToken)
                        append("token_type", config.getAccessTokenType())
                        append("asymmetric_jwt", "true")
                    }
                ).body()

                val mapped = authResponse.mapToDomain()
                val accessToken = mapped.accessToken ?: ""
                val newRefresh = mapped.refreshToken ?: ""

                if (accessToken.isNotEmpty() && newRefresh.isNotEmpty()) {
                    prefs.accessToken = accessToken
                    prefs.refreshToken = newRefresh
                    prefs.accessTokenExpiresAt = mapped.getTokenExpiryTime()
                    accessToken
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        if (newToken != null) {
            // Retry with new token
            request.headers.remove("Authorization")
            request.header("Authorization", "${config.getAccessTokenType()} $newToken")
            execute(request)
        } else {
            kotlinx.coroutines.runBlocking { appNotifier.send(LogoutEvent(true)) }
            originalCall
        }
    }
}
