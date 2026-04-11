package org.openedx.profile.data.repository

import org.openedx.profile.domain.model.Account

data class ImageBody(
    val bytes: ByteArray,
    val extension: String,
)

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
