package org.openedx.profile.data.repository

import org.openedx.profile.domain.model.Account

interface ProfileRepository {
    suspend fun getAccount(): Account
    suspend fun getAccount(username: String): Account
    fun getCachedAccount(): Account?
    suspend fun updateAccount(fields: Map<String, Any?>): Account
    suspend fun setProfileImage(file: Any, mimeType: String)
    suspend fun deleteProfileImage()
    suspend fun deactivateAccount(password: String)
    suspend fun logout()
}
