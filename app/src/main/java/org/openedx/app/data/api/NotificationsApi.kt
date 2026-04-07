package org.openedx.app.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters

class NotificationsApi(private val client: HttpClient) {
    suspend fun syncFirebaseToken(token: String, active: Boolean = true) {
        client.submitForm("/api/mobile/v4/notifications/create-token/", parameters {
            append("registration_id", token)
            append("active", active.toString())
        })
    }
}
