package org.openedx.profile.data.repository

import org.openedx.profile.domain.model.Account

data class ImageBody(
    val bytes: ByteArray,
    val extension: String,
    val mimeType: String = mimeTypeForExtension(extension),
)

private fun mimeTypeForExtension(extension: String): String = when (extension.lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "gif" -> "image/gif"
    "heic" -> "image/heic"
    "webp" -> "image/webp"
    else -> "image/jpeg"
}

interface ProfileRepository {
    suspend fun getAccount(): Account
    suspend fun getAccount(username: String): Account
    fun getCachedAccount(): Account?
    suspend fun updateAccount(fields: Map<String, Any?>): Account
    suspend fun setProfileImage(imageBody: ImageBody)
    suspend fun deleteProfileImage()
    suspend fun deactivateAccount(password: String)
    suspend fun logout()
}
