package org.openedx.profile.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openedx.core.ApiConstants
import org.openedx.profile.data.model.Account

class ProfileApi(private val client: HttpClient) {

    suspend fun revokeAccessToken(
        clientId: String?,
        token: String?,
        tokenTypeHint: String?,
    ): HttpResponse {
        return client.submitForm(ApiConstants.URL_REVOKE_TOKEN, parameters {
            clientId?.let { append("client_id", it) }
            token?.let { append("token", it) }
            tokenTypeHint?.let { append("token_type_hint", it) }
        })
    }

    suspend fun getAccount(username: String): Account {
        return client.get("/api/user/v1/accounts/$username").body()
    }

    suspend fun updateAccount(username: String, fields: Map<String, Any?>): Account {
        val jsonBody = buildJsonObject {
            for ((key, value) in fields) {
                when (value) {
                    null -> put(key, JsonNull)
                    is String -> put(key, JsonPrimitive(value))
                    is Number -> put(key, JsonPrimitive(value))
                    is Boolean -> put(key, JsonPrimitive(value))
                    else -> put(key, JsonPrimitive(value.toString()))
                }
            }
        }
        return client.patch("/api/user/v1/accounts/$username") {
            header("Cache-Control", "no-cache")
            contentType(ContentType("application", "merge-patch+json"))
            setBody(jsonBody)
        }.body()
    }

    suspend fun setProfileImage(
        username: String?,
        contentDisposition: String?,
        mobile: Boolean = true,
        fileBytes: ByteArray?,
    ): HttpResponse {
        return client.post("/api/user/v1/accounts/$username/image") {
            header("Cache-Control", "no-cache")
            contentDisposition?.let { header("Content-Disposition", it) }
            parameter("mobile", mobile)
            fileBytes?.let {
                contentType(ContentType.Application.OctetStream)
                setBody(it)
            }
        }
    }

    suspend fun deleteProfileImage(username: String?): HttpResponse {
        return client.delete("/api/user/v1/accounts/$username/image") {
            header("Cache-Control", "no-cache")
        }
    }

    suspend fun deactivateAccount(password: String): HttpResponse {
        return client.submitForm("/api/user/v1/accounts/deactivate_logout/", parameters {
            append("password", password)
        })
    }
}
