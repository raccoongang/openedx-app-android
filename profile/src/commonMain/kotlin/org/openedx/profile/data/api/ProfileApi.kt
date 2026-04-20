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
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openedx.core.ApiConstants
import org.openedx.core.domain.model.LanguageProficiency
import org.openedx.profile.data.model.Account
import org.openedx.profile.domain.model.Account as DomainAccount

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
                put(key, value.toJsonElement())
            }
        }
        return client.patch("/api/user/v1/accounts/$username") {
            header("Cache-Control", "no-cache")
            contentType(ContentType("application", "merge-patch+json"))
            setBody(jsonBody)
        }.body()
    }

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is DomainAccount.Privacy -> JsonPrimitive(name.lowercase())
        is Enum<*> -> JsonPrimitive(name.lowercase())
        is LanguageProficiency -> buildJsonObject {
            put("code", JsonPrimitive(code))
        }
        is List<*> -> JsonArray(map { it.toJsonElement() })
        is Map<*, *> -> buildJsonObject {
            this@toJsonElement.forEach { (k, v) ->
                put(k.toString(), v.toJsonElement())
            }
        }
        else -> JsonPrimitive(toString())
    }

    suspend fun setProfileImage(
        username: String?,
        contentDisposition: String?,
        contentType: String,
        mobile: Boolean = true,
        fileBytes: ByteArray?,
    ): HttpResponse {
        val parsedType = ContentType.parse(contentType)
        val response = client.post("/api/user/v1/accounts/$username/image") {
            header("Cache-Control", "no-cache")
            contentDisposition?.let { header(HttpHeaders.ContentDisposition, it) }
            parameter("mobile", mobile)
            if (fileBytes != null) {
                setBody(ByteArrayContent(fileBytes, parsedType))
            }
        }
        if (!response.status.isSuccess()) {
            throw io.ktor.client.plugins.ResponseException(response, "Profile image upload failed: ${response.status}")
        }
        return response
    }

    suspend fun deleteProfileImage(username: String?): HttpResponse {
        val response = client.delete("/api/user/v1/accounts/$username/image") {
            header("Cache-Control", "no-cache")
        }
        if (!response.status.isSuccess()) {
            throw io.ktor.client.plugins.ResponseException(response, "Profile image delete failed: ${response.status}")
        }
        return response
    }

    suspend fun deactivateAccount(password: String): HttpResponse {
        return client.submitForm("/api/user/v1/accounts/deactivate_logout/", parameters {
            append("password", password)
        })
    }
}
