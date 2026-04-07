package org.openedx.core.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import org.openedx.core.ApiConstants

class CookiesApi(private val client: HttpClient) {
    suspend fun userCookies(): HttpResponse {
        return client.post(ApiConstants.URL_LOGIN)
    }
}
